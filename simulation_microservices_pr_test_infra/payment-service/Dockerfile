# Base image Java 
FROM eclipse-temurin:21-jdk-jammy

# Créer le dossier de travail dans le container
WORKDIR /app

# Copier le code Java dans le container
COPY . .

# Compiler le fichier Java
RUN javac PaymentService.java

# Commande pour exécuter le service
CMD ["java", "PaymentService"]
