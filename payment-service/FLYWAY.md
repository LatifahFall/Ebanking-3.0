# Flyway Database Migrations

## Configuration

Flyway est configuré dans `application.yml` avec les propriétés suivantes :

```yaml
spring:
  flyway:
    enabled: ${FLYWAY_ENABLED:true}
    locations: classpath:db/migration
    baseline-on-migrate: true
    validate-on-migrate: true
```

## Migrations disponibles

### V1__create_payments_table.sql
Crée la table `payments` avec tous les champs nécessaires et les index de performance.

### V2__create_payment_rules_table.sql
Crée la table `payment_rules` pour gérer les règles métier de paiement.

### V3__add_updated_at_trigger.sql
Ajoute un trigger automatique pour mettre à jour le champ `updated_at`.

## Tester les migrations en local

### Prérequis

1. PostgreSQL installé et démarré
2. Base de données créée :
   ```sql
   CREATE DATABASE ebanking_payment;
   ```

3. Configuration dans `application.yml` ou variables d'environnement :
   ```yaml
   spring:
     datasource:
       url: jdbc:postgresql://localhost:5432/ebanking_payment
       username: postgres
       password: postgres
   ```

### Méthode 1 : Via Spring Boot

1. Démarrer l'application :
   ```bash
   mvn spring-boot:run
   ```

2. Flyway exécutera automatiquement les migrations au démarrage.

3. Vérifier les migrations dans la base de données :
   ```sql
   SELECT * FROM flyway_schema_history;
   ```

### Méthode 2 : Via Flyway CLI

1. Installer Flyway CLI : https://flywaydb.org/documentation/usage/commandline/

2. Configurer dans `flyway.conf` :
   ```properties
   flyway.url=jdbc:postgresql://localhost:5432/ebanking_payment
   flyway.user=postgres
   flyway.password=postgres
   flyway.locations=filesystem:src/main/resources/db/migration
   ```

3. Exécuter les migrations :
   ```bash
   flyway migrate
   ```

4. Vérifier le statut :
   ```bash
   flyway info
   ```

### Méthode 3 : Via Docker (Recommandé pour tests)

1. Démarrer PostgreSQL avec Docker :
   ```bash
   docker run -d \
     --name postgres-payment \
     -e POSTGRES_DB=ebanking_payment \
     -e POSTGRES_USER=postgres \
     -e POSTGRES_PASSWORD=postgres \
     -p 5432:5432 \
     postgres:latest
   ```

2. Démarrer l'application Spring Boot (les migrations s'exécuteront automatiquement)

3. Vérifier les tables créées :
   ```bash
   docker exec -it postgres-payment psql -U postgres -d ebanking_payment -c "\dt"
   ```

## Vérifier que les migrations ont réussi

### Via psql

```sql
-- Se connecter à la base de données
\c ebanking_payment

-- Vérifier que les tables existent
\dt

-- Vérifier l'historique Flyway
SELECT * FROM flyway_schema_history ORDER BY installed_rank;

-- Vérifier la structure de la table payments
\d payments

-- Vérifier la structure de la table payment_rules
\d payment_rules
```

### Via application logs

Les logs Spring Boot afficheront :
```
Flyway Migrations DDL: ...
Flyway Migrations DML: ...
Flyway Migrations Rerun: ...
```

## Commandes utiles

### Réinitialiser les migrations (DEV uniquement)

⚠️ **ATTENTION : Ne jamais faire en production !**

```sql
-- Supprimer toutes les tables
DROP TABLE IF EXISTS flyway_schema_history CASCADE;
DROP TABLE IF EXISTS payments CASCADE;
DROP TABLE IF EXISTS payment_rules CASCADE;
DROP FUNCTION IF EXISTS update_updated_at_column() CASCADE;

-- Redémarrer l'application pour réexécuter les migrations
```

### Vérifier les migrations en attente

```bash
flyway info
```

### Réparer Flyway (si nécessaire)

```bash
flyway repair
```

## Bonnes pratiques

1. ✅ Toujours tester les migrations en local avant de commit
2. ✅ Ne jamais modifier une migration déjà appliquée (créer une nouvelle migration)
3. ✅ Toujours faire des backups avant migrations en production
4. ✅ Utiliser des transactions pour les migrations complexes
5. ✅ Documenter les migrations importantes dans les commentaires SQL

## Problèmes courants

### Erreur : "Found non-empty schema without metadata table"

Solution : Activer `baseline-on-migrate: true` (déjà configuré)

### Erreur : "Migration checksum mismatch"

Solution : 
- Vérifier que le fichier n'a pas été modifié
- Si intentionnel, réparer avec `flyway repair`

### Erreur : "Connection refused"

Solution : Vérifier que PostgreSQL est démarré et que les credentials sont corrects

