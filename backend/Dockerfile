# syntax=docker/dockerfile:1

# ---- Stage 1: build the jar ----
FROM eclipse-temurin:21-jdk AS build
WORKDIR /workspace

# Cache dependencies first.
COPY gradlew settings.gradle build.gradle ./
COPY gradle ./gradle
RUN ./gradlew --no-daemon dependencies > /dev/null 2>&1 || true

# Build.
COPY src ./src
RUN ./gradlew --no-daemon clean bootJar -x test

# ---- Stage 2: slim runtime ----
FROM eclipse-temurin:21-jre AS runtime
WORKDIR /app

# Run as a non-root user.
RUN groupadd --system gk && useradd --system --gid gk --home /app gk
COPY --from=build /workspace/build/libs/*.jar /app/app.jar
USER gk

EXPOSE 8080
# No secrets baked in — all config comes from env at runtime.
ENTRYPOINT ["java", "-jar", "/app/app.jar"]
