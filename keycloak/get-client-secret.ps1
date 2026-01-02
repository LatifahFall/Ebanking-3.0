# Get Keycloak Client Secret - Simple Version
# Quick script to retrieve the client secret from Keycloak

param(
    [string]$KeycloakUrl = "http://localhost:8180",
    [string]$AdminUser = "admin",
    [string]$AdminPassword = "admin",
    [string]$RealmName = "bank-realm",
    [string]$ClientId = "auth-service"
)

Write-Host ""
Write-Host "Retrieving Keycloak Client Secret..." -ForegroundColor Cyan
Write-Host ""

try {
    # Get admin token
    Write-Host "Step 1: Authenticating..." -ForegroundColor Yellow
    $tokenResponse = Invoke-RestMethod `
        -Uri "$KeycloakUrl/realms/master/protocol/openid-connect/token" `
        -Method Post `
        -ContentType "application/x-www-form-urlencoded" `
        -Body @{
            username = $AdminUser
            password = $AdminPassword
            grant_type = "password"
            client_id = "admin-cli"
        } -ErrorAction Stop

    $adminToken = $tokenResponse.access_token
    Write-Host "Success: Authenticated" -ForegroundColor Green
    Write-Host ""

    # Get client UUID
    Write-Host "Step 2: Finding client..." -ForegroundColor Yellow
    $clients = Invoke-RestMethod `
        -Uri "$KeycloakUrl/admin/realms/$RealmName/clients?clientId=$ClientId" `
        -Headers @{"Authorization" = "Bearer $adminToken"} `
        -Method Get -ErrorAction Stop

    if ($clients.Count -eq 0) {
        Write-Host ""
        Write-Host "Error: Client '$ClientId' not found in realm '$RealmName'" -ForegroundColor Red
        Write-Host "Make sure Keycloak is configured properly." -ForegroundColor Yellow
        exit 1
    }

    $clientUuid = $clients[0].id
    Write-Host "Success: Client found" -ForegroundColor Green
    Write-Host ""

    # Get client secret
    Write-Host "Step 3: Retrieving secret..." -ForegroundColor Yellow
    $secretResponse = Invoke-RestMethod `
        -Uri "$KeycloakUrl/admin/realms/$RealmName/clients/$clientUuid/client-secret" `
        -Headers @{"Authorization" = "Bearer $adminToken"} `
        -Method Get -ErrorAction Stop

    $clientSecret = $secretResponse.value
    Write-Host "Success: Secret retrieved" -ForegroundColor Green
    Write-Host ""

    # Display result
    Write-Host "=========================================" -ForegroundColor Cyan
    Write-Host "Client Secret Retrieved!" -ForegroundColor Green
    Write-Host "=========================================" -ForegroundColor Cyan
    Write-Host ""
    Write-Host "Realm:     $RealmName" -ForegroundColor White
    Write-Host "Client ID: $ClientId" -ForegroundColor White
    Write-Host "Secret:    $clientSecret" -ForegroundColor Green
    Write-Host ""
    Write-Host "=========================================" -ForegroundColor Cyan
    Write-Host ""

    Write-Host "Copy this line to your .env file:" -ForegroundColor Cyan
    Write-Host ""
    Write-Host "KEYCLOAK_CLIENT_SECRET=$clientSecret" -ForegroundColor White
    Write-Host ""

    # Ask to update .env file
    $update = Read-Host "Update .env file automatically? (y/N)"

    if ($update -eq "y" -or $update -eq "Y") {
        $envFile = ".env"

        if (Test-Path $envFile) {
            $envContent = Get-Content $envFile
            $secretLinePattern = "KEYCLOAK_CLIENT_SECRET="
            $newSecretLine = "KEYCLOAK_CLIENT_SECRET=$clientSecret"

            $found = $false
            $newContent = @()

            foreach ($line in $envContent) {
                if ($line -match "^KEYCLOAK_CLIENT_SECRET=") {
                    $newContent += $newSecretLine
                    $found = $true
                } else {
                    $newContent += $line
                }
            }

            if (-not $found) {
                $newContent += $newSecretLine
            }

            $newContent | Set-Content $envFile
            Write-Host ""
            Write-Host "Success: .env file updated!" -ForegroundColor Green
        } else {
            Write-Host ""
            Write-Host "Warning: .env file not found" -ForegroundColor Yellow
            Write-Host "Creating .env file..." -ForegroundColor Yellow

            if (Test-Path ".env.example") {
                Copy-Item ".env.example" ".env"
                $envContent = Get-Content ".env"
                $newContent = @()

                foreach ($line in $envContent) {
                    if ($line -match "^KEYCLOAK_CLIENT_SECRET=") {
                        $newContent += "KEYCLOAK_CLIENT_SECRET=$clientSecret"
                    } else {
                        $newContent += $line
                    }
                }

                $newContent | Set-Content ".env"
                Write-Host "Success: .env created and updated!" -ForegroundColor Green
            } else {
                "KEYCLOAK_CLIENT_SECRET=$clientSecret" | Set-Content ".env"
                Write-Host "Success: .env created!" -ForegroundColor Green
            }
        }

        Write-Host ""
        Write-Host "Next step: Restart auth-service" -ForegroundColor Cyan
        Write-Host "docker-compose restart auth-service" -ForegroundColor Yellow
    }

    Write-Host ""

} catch {
    Write-Host ""
    Write-Host "Error retrieving client secret:" -ForegroundColor Red
    Write-Host $_.Exception.Message -ForegroundColor Red
    Write-Host ""
    Write-Host "Troubleshooting:" -ForegroundColor Yellow
    Write-Host "1. Make sure Keycloak is running: docker-compose ps" -ForegroundColor White
    Write-Host "2. Wait for Keycloak to be ready (can take 1-2 minutes)" -ForegroundColor White
    Write-Host "3. Check Keycloak logs: docker-compose logs keycloak" -ForegroundColor White
    Write-Host "4. Verify admin credentials are correct" -ForegroundColor White
    Write-Host ""
    exit 1
}