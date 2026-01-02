# =========================
# Étape 1 : Build
# =========================
FROM maven:latest AS build

WORKDIR /app

COPY pom.xml .
RUN mvn dependency:go-offline

COPY src ./src

RUN mvn clean package -DskipTests

# =========================
# Étape 2 : Runtime
# =========================
FROM eclipse-temurin:latest

WORKDIR /app

COPY --from=build /app/target/*.jar app.jar

EXPOSE 8081

ENV JAVA_OPTS="-Xms256m -Xmx512m"
ENV SPRING_PROFILES_ACTIVE=docker

ENTRYPOINT ["sh", "-c", "java $JAVA_OPTS -jar app.jar"]
