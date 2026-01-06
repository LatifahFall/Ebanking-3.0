# ====================================================================
# API Gateway - Quick Start Script
# ====================================================================
# Ce script build et démarre l'API Gateway en mode dev
# Usage: .\quick-start-gateway.ps1
# ====================================================================

$baseDir = "C:\Users\Hp\Desktop\rest\api-gateway"

Write-Host "=====================================================================" -ForegroundColor Cyan
Write-Host "API Gateway - Quick Start" -ForegroundColor Cyan
Write-Host "=====================================================================" -ForegroundColor Cyan
Write-Host ""

# Check if directory exists
if (-not (Test-Path $baseDir)) {
    Write-Host "❌ Directory not found: $baseDir" -ForegroundColor Red
    exit 1
}

Set-Location $baseDir

# Step 1: Build
Write-Host "Step 1: Building API Gateway..." -ForegroundColor Yellow
Write-Host "Running: mvn clean install" -ForegroundColor Gray
Write-Host ""

mvn clean install -DskipTests

if ($LASTEXITCODE -ne 0) {
    Write-Host "❌ Build failed!" -ForegroundColor Red
    exit 1
}

Write-Host "✅ Build successful!" -ForegroundColor Green
Write-Host ""

# Step 2: Configure environment
Write-Host "Step 2: Configuring environment (Dev Mode)..." -ForegroundColor Yellow

$env:KEYCLOAK_ENABLED = "false"
$env:SERVER_PORT = "8080"

# Microservices URLs - AJUSTEZ CES URLs SELON VOTRE CONFIG!
$env:AUTH_SERVICE_URL = "http://localhost:8080"
$env:USER_SERVICE_URL = "http://localhost:8081"
$env:ACCOUNT_SERVICE_URL = "http://localhost:8082"
$env:PAYMENT_SERVICE_URL = "http://localhost:8083"
$env:CRYPTO_SERVICE_URL = "http://localhost:8085"
$env:NOTIFICATION_SERVICE_URL = "http://localhost:8086"
$env:AUDIT_SERVICE_URL = "http://localhost:8087"
$env:ANALYTICS_SERVICE_URL = "http://localhost:8088"

# Redis (optional)
$env:REDIS_HOST = "localhost"
$env:REDIS_PORT = "6379"

Write-Host "Configuration:" -ForegroundColor Cyan
Write-Host "  KEYCLOAK_ENABLED = false (Dev mode - No auth)" -ForegroundColor Gray
Write-Host "  SERVER_PORT = 8080" -ForegroundColor Gray
Write-Host "  USER_SERVICE_URL = $env:USER_SERVICE_URL" -ForegroundColor Gray
Write-Host "  ACCOUNT_SERVICE_URL = $env:ACCOUNT_SERVICE_URL" -ForegroundColor Gray
Write-Host "  PAYMENT_SERVICE_URL = $env:PAYMENT_SERVICE_URL" -ForegroundColor Gray
Write-Host ""

# Step 3: Check port availability
Write-Host "Step 3: Checking port availability..." -ForegroundColor Yellow

$port = 8080
$connection = Test-NetConnection -ComputerName localhost -Port $port -InformationLevel Quiet -WarningAction SilentlyContinue

if ($connection) {
    Write-Host "⚠️  Port $port is already in use!" -ForegroundColor Yellow
    Write-Host "Do you want to continue anyway? (Y/N)" -ForegroundColor Yellow
    $response = Read-Host
    
    if ($response -ne "Y" -and $response -ne "y") {
        Write-Host "Cancelled by user." -ForegroundColor Red
        exit 0
    }
} else {
    Write-Host "✅ Port $port is available" -ForegroundColor Green
}

Write-Host ""

# Step 4: Start Gateway
Write-Host "=====================================================================" -ForegroundColor Cyan
Write-Host "Starting API Gateway..." -ForegroundColor Yellow
Write-Host "=====================================================================" -ForegroundColor Cyan
Write-Host ""
Write-Host "Gateway will be available at: http://localhost:8080" -ForegroundColor Green
Write-Host "Health check: http://localhost:8080/actuator/health" -ForegroundColor Green
Write-Host "Routes info: http://localhost:8080/actuator/gateway/routes" -ForegroundColor Green
Write-Host ""
Write-Host "Press Ctrl+C to stop the gateway" -ForegroundColor Yellow
Write-Host ""

mvn spring-boot:run
