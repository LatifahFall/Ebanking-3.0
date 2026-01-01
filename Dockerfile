# =============================================================================
# Multi-stage Dockerfile pour account-service (Spring Boot 3 + Java 17)
# Optimisé pour sécurité, taille, performance et observabilité
# =============================================================================

# -------------------------------------------------------------------------
# Stage 1: Build avec Maven officiel
# -------------------------------------------------------------------------
FROM maven:3.9.6-eclipse-temurin-17-alpine AS build

LABEL maintainer="e-banking-team@bank.com"
LABEL stage="build"

WORKDIR /app

# Copie du pom.xml pour profiter du cache des dépendances
COPY pom.xml .

# Télécharge les dépendances (couche cachée si pom.xml inchangé)
RUN mvn dependency:go-offline -B

# Copie du code source
COPY src ./src

# Build du JAR (skip tests — déjà faits en CI)
RUN mvn clean package -DskipTests -B

# -------------------------------------------------------------------------
# Stage 2: Runtime minimal avec JRE uniquement
# -------------------------------------------------------------------------
FROM eclipse-temurin:17-jre-alpine AS runtime

LABEL maintainer="e-banking-team@bank.com"
LABEL stage="runtime"

# Créer un utilisateur non-root (sécurité)
ARG UID=1000
ARG GID=1000
RUN addgroup -g ${GID} -S spring && \
    adduser -u ${UID} -S -G spring spring

WORKDIR /app

# Copier le JAR construit
COPY --from=build --chown=spring:spring /app/target/account-service-1.0.0.jar app.jar

# Utilisateur non-root
USER spring:spring

# Port de l'application (comme dans application.yml)
EXPOSE 8082

# Healthcheck sur actuator
HEALTHCHECK --interval=30s --timeout=5s --start-period=60s --retries=3 \
    CMD wget --no-verbose --tries=1 --spider http://localhost:8082/actuator/health || exit 1

# JVM tuning optimisé pour conteneurs
ENV JAVA_OPTS="\
    -XX:+UseContainerSupport \
    -XX:MaxRAMPercentage=75.0 \
    -XX:+UseZGC \
    -Djava.security.egd=file:/dev/./urandom \
    -Dspring.jmx.enabled=false \
    -Dfile.encoding=UTF-8"

# Démarrage
ENTRYPOINT ["sh", "-c", "java $JAVA_OPTS -jar app.jar"]