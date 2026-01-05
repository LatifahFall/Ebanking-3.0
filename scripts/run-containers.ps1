# Script PowerShell pour démarrer Postgres, Zookeeper, Kafka, InfluxDB et l'application
# Usage: PowerShell -ExecutionPolicy Bypass -File .\scripts\run-containers.ps1

param()

Write-Host "Vérification de Docker..."
if (-not (Get-Command docker -ErrorAction SilentlyContinue)) {
    Write-Error "Docker CLI introuvable. Installez Docker Desktop ou ajoutez docker au PATH puis relancez ce script."
    exit 1
}

$network = "analytics-net"
Write-Host "Création du réseau Docker (si nécessaire): $network"
docker network inspect $network >/dev/null 2>&1 || docker network create $network

function Remove-IfExists($name) {
    $c = docker ps -a --filter "name=^/$name$" --format "{{.ID}}"
    if ($c) { docker rm -f $name | Out-Null }
}

# Postgres
Remove-IfExists "analytics-postgres"
Write-Host "Lancement Postgres..."
docker run -d --name analytics-postgres --network $network `
  -e POSTGRES_DB=analytics_db -e POSTGRES_USER=postgres -e POSTGRES_PASSWORD=gabrielle `
  -p 5432:5432 -v pgdata:/var/lib/postgresql/data postgres:15

Start-Sleep -Seconds 5

# Zookeeper
Remove-IfExists "zookeeper"
Write-Host "Lancement Zookeeper..."
docker run -d --name zookeeper --network $network `
  -e ALLOW_ANONYMOUS_LOGIN=yes -p 2181:2181 bitnami/zookeeper:latest

Start-Sleep -Seconds 5

# Kafka
Remove-IfExists "kafka"
Write-Host "Lancement Kafka..."
docker run -d --name kafka --network $network `
  -e KAFKA_CFG_ZOOKEEPER_CONNECT=zookeeper:2181 `
  -e ALLOW_PLAINTEXT_LISTENER=yes `
  -e KAFKA_CFG_LISTENERS=PLAINTEXT://0.0.0.0:9092 `
  -e KAFKA_CFG_ADVERTISED_LISTENERS=PLAINTEXT://kafka:9092 `
  -p 9092:9092 bitnami/kafka:latest

Start-Sleep -Seconds 10

# InfluxDB
Remove-IfExists "influxdb"
Write-Host "Lancement InfluxDB..."
docker run -d --name influxdb --network $network -p 8086:8086 `
  -e DOCKER_INFLUXDB_INIT_MODE=setup `
  -e DOCKER_INFLUXDB_INIT_USERNAME=admin `
  -e DOCKER_INFLUXDB_INIT_PASSWORD=adminpass `
  -e DOCKER_INFLUXDB_INIT_ORG=banking `
  -e DOCKER_INFLUXDB_INIT_BUCKET=analytics `
  -e DOCKER_INFLUXDB_INIT_ADMIN_TOKEN=my-super-secret-token influxdb:2.6

Start-Sleep -Seconds 10

# Build de l'image de l'application
Write-Host "Construction de l'image Docker de l'application (peut prendre quelques minutes)..."
docker build -t analytics-service:latest .

# Application
Remove-IfExists "analytics-app"
Write-Host "Lancement de l'application..."
docker run -d --name analytics-app --network $network -p 8087:8087 `
  -e DB_HOST=analytics-postgres -e DB_PORT=5432 -e DB_NAME=analytics_db `
  -e DB_USER=postgres -e DB_PASSWORD=gabrielle `
  -e KAFKA_BOOTSTRAP_SERVERS=kafka:9092 `
  -e INFLUXDB_URL=http://influxdb:8086 -e INFLUXDB_TOKEN=my-super-secret-token `
  -e INFLUXDB_ORG=banking -e INFLUXDB_BUCKET=analytics analytics-service:latest

Start-Sleep -Seconds 10

# Création des topics Kafka
$topics = @(
    "auth.events",
    "user.created",
    "user.updated",
    "user.activated",
    "user.deactivated",
    "kyc.status.changed",
    "client.assigned",
    "client.unassigned",
    "account.created",
    "account.updated",
    "account.balance.changed",
    "account.suspended",
    "account.closed",
    "transaction.completed",
    "payment.initiated",
    "payment.completed",
    "payment.failed",
    "payment.reversed",
    "fraud.detected",
    "crypto.transaction",
    "notification.status",
    "notification.audit"
)

Write-Host "Création des topics Kafka..."
foreach ($t in $topics) {
    Write-Host "  Création: $t"
    docker exec kafka kafka-topics.sh --create --bootstrap-server kafka:9092 --replication-factor 1 --partitions 3 --topic $t 2>$null
}

Write-Host "Terminé. Vérifiez les logs avec : docker logs -f analytics-app"
