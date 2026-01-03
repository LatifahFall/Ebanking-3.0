# ====================================
# Stage 1: Build avec Maven
# ====================================
FROM maven:latest AS build
WORKDIR /build

# Copy dependency definitions (for better layer caching)
COPY pom.xml .

# Download dependencies (cached if pom.xml unchanged)
RUN mvn dependency:go-offline -B

# Copy source code
COPY src ./src

# Build application (skip tests for faster builds)
RUN mvn clean package -DskipTests -B

# ====================================
# Stage 2: Runtime optimized image
# ====================================
FROM eclipse-temurin:latest
WORKDIR /app

# Install curl for health checks (Debian-based)
RUN apt-get update && apt-get install -y curl && rm -rf /var/lib/apt/lists/*

# Create non-root user for security
RUN groupadd -g 1001 appuser && \
    useradd -r -u 1001 -g appuser appuser

# Copy JAR from build stage
COPY --from=build /build/target/notification-service-*.jar app.jar

# Change ownership to non-root user
RUN chown -R appuser:appuser /app

# Switch to non-root user
USER appuser

# Health check for Kubernetes
HEALTHCHECK --interval=30s --timeout=3s --start-period=40s --retries=3 \
  CMD curl -f http://localhost:8084/actuator/health || exit 1

# Expose port
EXPOSE 8084

# JVM optimizations for containers
ENV JAVA_OPTS="-XX:+UseContainerSupport -XX:MaxRAMPercentage=75.0 -XX:+ExitOnOutOfMemoryError"

# Run application
ENTRYPOINT ["sh", "-c", "java $JAVA_OPTS -jar app.jar"]

