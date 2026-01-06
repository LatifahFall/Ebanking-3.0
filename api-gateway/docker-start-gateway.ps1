# ====================================================================
# API Gateway - Docker Build and Run Script
# ====================================================================
# Ce script build l'image Docker et démarre le Gateway avec Docker Compose
# Usage: .\docker-start-gateway.ps1
# ====================================================================

$baseDir = "C:\Users\Hp\Desktop\rest\api-gateway"

Write-Host "=====================================================================" -ForegroundColor Cyan
Write-Host "API Gateway - Docker Build & Run" -ForegroundColor Cyan
Write-Host "=====================================================================" -ForegroundColor Cyan
Write-Host ""

# Check if directory exists
if (-not (Test-Path $baseDir)) {
    Write-Host "❌ Directory not found: $baseDir" -ForegroundColor Red
    exit 1
}

Set-Location $baseDir

# Step 1: Check .env file
Write-Host "Step 1: Checking configuration..." -ForegroundColor Yellow

if (-not (Test-Path ".env")) {
    Write-Host "⚠️  .env file not found, creating from template..." -ForegroundColor Yellow
    Copy-Item ".env.example" ".env"
    Write-Host "✅ Created .env file" -ForegroundColor Green
    Write-Host "💡 Edit .env to configure your microservices URLs" -ForegroundColor Cyan
    Write-Host ""
    
    Write-Host "Do you want to edit .env now? (Y/N)" -ForegroundColor Yellow
    $response = Read-Host
    
    if ($response -eq "Y" -or $response -eq "y") {
        notepad .env
        Write-Host "Press Enter when done editing..." -ForegroundColor Yellow
        Read-Host
    }
} else {
    Write-Host "✅ .env file found" -ForegroundColor Green
}

Write-Host ""

# Step 2: Build Docker image
Write-Host "Step 2: Building Docker image..." -ForegroundColor Yellow
Write-Host "Running: docker-compose build" -ForegroundColor Gray
Write-Host ""

docker-compose build

if ($LASTEXITCODE -ne 0) {
    Write-Host "❌ Docker build failed!" -ForegroundColor Red
    exit 1
}

Write-Host "✅ Docker image built successfully!" -ForegroundColor Green
Write-Host ""

# Step 3: Start services
Write-Host "Step 3: Starting services..." -ForegroundColor Yellow
Write-Host "Running: docker-compose up -d" -ForegroundColor Gray
Write-Host ""

docker-compose up -d

if ($LASTEXITCODE -ne 0) {
    Write-Host "❌ Failed to start services!" -ForegroundColor Red
    exit 1
}

Write-Host "✅ Services started successfully!" -ForegroundColor Green
Write-Host ""

# Step 4: Wait for health check
Write-Host "Step 4: Waiting for API Gateway to be ready..." -ForegroundColor Yellow

$maxAttempts = 30
$attempt = 0
$healthy = $false

while ($attempt -lt $maxAttempts -and -not $healthy) {
    $attempt++
    Write-Host "  Attempt $attempt/$maxAttempts..." -ForegroundColor Gray
    
    try {
        $response = Invoke-WebRequest -Uri "http://localhost:8080/actuator/health" -Method GET -TimeoutSec 5 -UseBasicParsing
        
        if ($response.StatusCode -eq 200) {
            $healthy = $true
            Write-Host "✅ API Gateway is healthy!" -ForegroundColor Green
        }
    } catch {
        Start-Sleep -Seconds 2
    }
}

if (-not $healthy) {
    Write-Host "⚠️  API Gateway health check timed out" -ForegroundColor Yellow
    Write-Host "Check logs with: docker logs api-gateway" -ForegroundColor Gray
} else {
    Write-Host ""
    Write-Host "=====================================================================" -ForegroundColor Cyan
    Write-Host "API Gateway is running!" -ForegroundColor Green
    Write-Host "=====================================================================" -ForegroundColor Cyan
    Write-Host ""
    Write-Host "Gateway URL: http://localhost:8080" -ForegroundColor Green
    Write-Host "Health check: http://localhost:8080/actuator/health" -ForegroundColor Green
    Write-Host "Routes info: http://localhost:8080/actuator/gateway/routes" -ForegroundColor Green
    Write-Host ""
    Write-Host "Useful commands:" -ForegroundColor Yellow
    Write-Host "  View logs: docker logs -f api-gateway" -ForegroundColor Gray
    Write-Host "  Stop: docker-compose down" -ForegroundColor Gray
    Write-Host "  Restart: docker-compose restart" -ForegroundColor Gray
    Write-Host ""
}

# Step 5: Show running containers
Write-Host "Running containers:" -ForegroundColor Yellow
docker ps --filter "name=api-gateway" --format "table {{.Names}}\t{{.Status}}\t{{.Ports}}"

Write-Host ""
