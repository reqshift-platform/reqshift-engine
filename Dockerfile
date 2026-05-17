# Multi-stage build that produces a fully static ReqShift binary
# linked against musl libc, then ships it in a FROM scratch image.
# Final image is roughly the size of the binary itself (~30 MB).

ARG MUSL_VERSION=1.2.5
ARG ZLIB_VERSION=1.3.1
ARG MAVEN_VERSION=3.9.9

# --- builder ----------------------------------------------------------
FROM ghcr.io/graalvm/native-image-community:25-ol9 AS builder

ARG MUSL_VERSION
ARG ZLIB_VERSION
ARG MAVEN_VERSION

RUN microdnf install -y gcc make tar gzip findutils \
    && microdnf clean all

# Apache Maven 3.9.9 (the base image ships 3.6.3, which is too old for us).
RUN curl -fsSL -o /tmp/maven.tar.gz "https://archive.apache.org/dist/maven/maven-3/${MAVEN_VERSION}/binaries/apache-maven-${MAVEN_VERSION}-bin.tar.gz" \
    && tar -xzf /tmp/maven.tar.gz -C /opt \
    && rm /tmp/maven.tar.gz \
    && ln -s "/opt/apache-maven-${MAVEN_VERSION}/bin/mvn" /usr/local/bin/mvn

# Build musl libc from source (musl.libc.org is more reliable than musl.cc).
# We then symlink the wrapper under the triplet name GraalVM expects
# (x86_64-linux-musl-gcc).
RUN curl -fsSL -o /tmp/musl.tar.gz "https://musl.libc.org/releases/musl-${MUSL_VERSION}.tar.gz" \
    && tar -xzf /tmp/musl.tar.gz -C /tmp \
    && rm /tmp/musl.tar.gz \
    && cd "/tmp/musl-${MUSL_VERSION}" \
    && ./configure --prefix=/opt/musl --disable-shared \
    && make -j"$(nproc)" \
    && make install \
    && cd / \
    && rm -rf "/tmp/musl-${MUSL_VERSION}" \
    && ln -s /opt/musl/bin/musl-gcc /opt/musl/bin/x86_64-linux-musl-gcc

ENV PATH=/opt/musl/bin:$PATH
ENV LIBRARY_PATH=/opt/musl/lib

# Static zlib built with the musl toolchain. GraalVM links against it.
RUN curl -fsSL -o /tmp/zlib.tar.gz "https://github.com/madler/zlib/releases/download/v${ZLIB_VERSION}/zlib-${ZLIB_VERSION}.tar.gz" \
    && tar -xzf /tmp/zlib.tar.gz -C /tmp \
    && rm /tmp/zlib.tar.gz \
    && cd "/tmp/zlib-${ZLIB_VERSION}" \
    && CC=x86_64-linux-musl-gcc ./configure --prefix=/opt/musl --static \
    && make -j"$(nproc)" \
    && make install \
    && cd / \
    && rm -rf "/tmp/zlib-${ZLIB_VERSION}"

WORKDIR /build

# Warm the Maven dependency cache before pulling the full source tree.
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
