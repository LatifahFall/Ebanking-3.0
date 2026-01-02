Write-Host "Starting Auth Service..." -ForegroundColor Green

if (-not (Get-Command docker -ErrorAction SilentlyContinue)) {
    Write-Host "Docker is not installed" -ForegroundColor Red
    exit 1
}

if (-not (Get-Command docker-compose -ErrorAction SilentlyContinue)) {
    Write-Host "Docker Compose is not installed" -ForegroundColor Red
    exit 1
}

Set-Location -Path (Split-Path -Parent $PSScriptRoot)

Write-Host "Current directory: $(Get-Location)" -ForegroundColor Cyan

Write-Host "`nStopping existing containers..." -ForegroundColor Yellow
docker-compose down

Write-Host "`nBuilding and starting services..." -ForegroundColor Cyan
docker-compose up -d --build

if ($LASTEXITCODE -ne 0) {
    Write-Host "Error while starting services" -ForegroundColor Red
    exit 1
}

Start-Sleep -Seconds 30

Write-Host "`nServices status:" -ForegroundColor Green
docker-compose ps

Write-Host "`nAuth Service ready!" -ForegroundColor Green
Write-Host "Auth Service: http://localhost:8081" -ForegroundColor Cyan
Write-Host "Keycloak: http://localhost:8180" -ForegroundColor Cyan
Write-Host "`nLogs: docker-compose logs -f auth-service" -ForegroundColor Yellow
