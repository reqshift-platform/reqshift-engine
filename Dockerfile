# Multi-stage build that produces a fully static ReqShift binary
# linked against musl libc, then ships it in a FROM scratch image.
# Final image is roughly the size of the binary itself (~25 MB).

ARG MUSL_VERSION=1.2.5
ARG ZLIB_VERSION=1.3.1

# --- builder ----------------------------------------------------------
FROM ghcr.io/graalvm/native-image-community:25-ol9 AS builder

ARG MUSL_VERSION
ARG ZLIB_VERSION

RUN microdnf install -y maven gcc make tar gzip findutils \
    && microdnf clean all

# Build musl libc from source. GraalVM static-with-musl needs musl-gcc on the PATH.
WORKDIR /opt
RUN curl -fsSL -o musl.tar.gz "https://musl.libc.org/releases/musl-${MUSL_VERSION}.tar.gz" \
    && tar -xzf musl.tar.gz \
    && rm musl.tar.gz \
    && cd "musl-${MUSL_VERSION}" \
    && ./configure --prefix=/opt/musl --disable-shared \
    && make -j"$(nproc)" \
    && make install \
    && cd .. \
    && rm -rf "musl-${MUSL_VERSION}"

ENV PATH=/opt/musl/bin:$PATH

# Build zlib statically with musl-gcc; GraalVM links against it.
# We pull from GitHub releases instead of zlib.net to avoid the moving
# /fossils/ path once a new release ships.
RUN curl -fsSL -o zlib.tar.gz "https://github.com/madler/zlib/releases/download/v${ZLIB_VERSION}/zlib-${ZLIB_VERSION}.tar.gz" \
    && tar -xzf zlib.tar.gz \
    && rm zlib.tar.gz \
    && cd "zlib-${ZLIB_VERSION}" \
    && CC=musl-gcc ./configure --prefix=/opt/musl --static \
    && make -j"$(nproc)" \
    && make install \
    && cd .. \
    && rm -rf "zlib-${ZLIB_VERSION}"

WORKDIR /build

# Warm the Maven dependency cache before pulling sources.
COPY pom.xml ./
COPY reqshift-bom/pom.xml reqshift-bom/
COPY reqshift-core/pom.xml reqshift-core/
COPY reqshift-rules/pom.xml reqshift-rules/
COPY reqshift-scoring/pom.xml reqshift-scoring/
COPY reqshift-output/pom.xml reqshift-output/
COPY reqshift-cli/pom.xml reqshift-cli/
RUN mvn -B -ntp -pl reqshift-cli -am dependency:go-offline -DskipTests || true

COPY . .
RUN mvn -B -ntp -Pnative-static -DskipTests -pl reqshift-cli -am package

# --- runtime ----------------------------------------------------------
FROM scratch

LABEL org.opencontainers.image.source="https://github.com/reqshift-platform/reqshift-engine"
LABEL org.opencontainers.image.description="OpenAPI analysis, scoring, and security engine"
LABEL org.opencontainers.image.licenses="Apache-2.0"

COPY --from=builder /build/reqshift-cli/target/reqshift /reqshift

WORKDIR /work
ENTRYPOINT ["/reqshift"]
