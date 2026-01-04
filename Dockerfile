# =========================
# Étape 1 : Build
# =========================
FROM maven:3.9-eclipse-temurin-21 AS build

WORKDIR /app

COPY pom.xml .
RUN mvn dependency:go-offline -B

COPY src ./src

# Ajouter le plugin Spring Boot explicitement
RUN mvn clean package -DskipTests spring-boot:repackage

# =========================
# Étape 2 : Runtime
# =========================
FROM eclipse-temurin:21-jre-alpine

WORKDIR /app

# Le JAR Spring Boot a toutes les dépendances incluses
COPY --from=build /app/target/auth-service-*.jar app.jar

EXPOSE 8081

# Simplifier : pas besoin de sh -c
ENTRYPOINT ["java", "-jar", "app.jar"]
