FROM maven:3.9.4-eclipse-temurin-17 AS build
WORKDIR /workspace

# Copy only what is needed for dependency resolution first to leverage layer cache
COPY pom.xml ./
COPY src ./src

# Build the application (skip tests to speed-up image builds; remove -DskipTests in CI if needed)
RUN mvn -B -DskipTests package

# Runtime stage
FROM eclipse-temurin:17-jre-jammy
WORKDIR /app

# Create a non-root user
RUN groupadd -r spring && useradd -r -g spring spring || true
USER spring:spring

# Copy the built jar
COPY --from=build /workspace/target/*.jar /app/app.jar

# Health check (use context-path /api/v1 as in application.yml)
HEALTHCHECK --interval=30s --timeout=3s --start-period=60s --retries=3 \
  CMD wget --no-verbose --tries=1 --spider http://localhost:8086/api/v1/actuator/health || exit 1

# Expose application port (use the port configured in application.yml)
EXPOSE 8086

# JVM options
ENV JAVA_OPTS="-Xms256m -Xmx512m -XX:+UseG1GC"

# Run application
ENTRYPOINT ["sh", "-c", "java $JAVA_OPTS -jar /app/app.jar"]
