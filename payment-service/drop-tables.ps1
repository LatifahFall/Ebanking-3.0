# Script PowerShell pour supprimer les tables et laisser Hibernate les recréer
# ATTENTION : Cela supprimera toutes les données des tables payments et qr_code_payment

$dbHost = "localhost"
$dbPort = "5433"
$dbName = "ebanking_payment"
$dbUser = "postgres"
$dbPassword = "root"

Write-Host "Connexion à la base de données..." -ForegroundColor Cyan
$connectionString = "Host=$dbHost;Port=$dbPort;Database=$dbName;Username=$dbUser;Password=$dbPassword"

try {
    # Utiliser psql si disponible
    $env:PGPASSWORD = $dbPassword
    $dropScript = @"
DROP TABLE IF EXISTS qr_code_payment CASCADE;
DROP TABLE IF EXISTS payments CASCADE;
"@
    
    Write-Host "Suppression des tables..." -ForegroundColor Yellow
    $dropScript | & psql -h $dbHost -p $dbPort -U $dbUser -d $dbName
    
    if ($LASTEXITCODE -eq 0) {
        Write-Host "✅ Tables supprimées avec succès!" -ForegroundColor Green
        Write-Host "Redémarrez l'application Spring Boot pour que Hibernate recrée les tables avec BIGINT." -ForegroundColor Cyan
    } else {
        Write-Host "❌ Erreur lors de la suppression des tables" -ForegroundColor Red
    }
} catch {
    Write-Host "❌ Erreur: $_" -ForegroundColor Red
    Write-Host "`nAlternative: Exécutez manuellement dans psql ou pgAdmin:" -ForegroundColor Yellow
    Write-Host "DROP TABLE IF EXISTS qr_code_payment CASCADE;" -ForegroundColor Gray
    Write-Host "DROP TABLE IF EXISTS payments CASCADE;" -ForegroundColor Gray
}

