# Multi-stage build for the ReqShift native CLI.
# Runtime is gcr.io/distroless/base-debian12 (~25 MB), maintained by Google,
# providing glibc and the few shared libs the GraalVM binary links against.
# Final image is around 60 MB and runs as a non-root user.

ARG MAVEN_VERSION=3.9.9

# --- builder ----------------------------------------------------------
FROM ghcr.io/graalvm/native-image-community:25-ol9 AS builder

ARG MAVEN_VERSION

# The base image ships Maven 3.6.3; the project enforces 3.9+.
RUN microdnf install -y tar gzip findutils \
    && microdnf clean all \
    && curl -fsSL -o /tmp/maven.tar.gz "https://archive.apache.org/dist/maven/maven-3/${MAVEN_VERSION}/binaries/apache-maven-${MAVEN_VERSION}-bin.tar.gz" \
    && tar -xzf /tmp/maven.tar.gz -C /opt \
    && rm /tmp/maven.tar.gz \
    && ln -s "/opt/apache-maven-${MAVEN_VERSION}/bin/mvn" /usr/local/bin/mvn

WORKDIR /build

# Warm the Maven dependency cache before copying the full source tree.
COPY pom.xml ./
COPY reqshift-bom/pom.xml reqshift-bom/
COPY reqshift-core/pom.xml reqshift-core/
COPY reqshift-rules/pom.xml reqshift-rules/
COPY reqshift-scoring/pom.xml reqshift-scoring/
COPY reqshift-output/pom.xml reqshift-output/
COPY reqshift-cli/pom.xml reqshift-cli/
RUN mvn -B -ntp -pl reqshift-cli -am dependency:go-offline -DskipTests || true

COPY . .
RUN mvn -B -ntp -Pnative -DskipTests -pl reqshift-cli -am package

# --- runtime ----------------------------------------------------------
FROM gcr.io/distroless/base-debian12:nonroot

LABEL org.opencontainers.image.source="https://github.com/reqshift-platform/reqshift-engine"
LABEL org.opencontainers.image.description="OpenAPI analysis, scoring, and security engine"
LABEL org.opencontainers.image.licenses="Apache-2.0"

COPY --from=builder /build/reqshift-cli/target/reqshift /usr/local/bin/reqshift

WORKDIR /work
ENTRYPOINT ["/usr/local/bin/reqshift"]
