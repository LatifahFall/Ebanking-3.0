# Script to fix SCSS double import issues
# Run this after stopping ng serve (Ctrl+C)

Write-Host "Fixing SCSS imports to prevent double declarations..." -ForegroundColor Cyan

# Fix theme files
Write-Host "`nUpdating theme files..." -ForegroundColor Yellow

$lightTheme = "src\app\themes\light-theme.scss"
if (Test-Path $lightTheme) {
    Write-Host "  $lightTheme" -ForegroundColor Green
    $content = Get-Content $lightTheme -Raw
    $content = $content -replace "@import './variables';", "@use './variables' as *;"
    Set-Content $lightTheme $content -NoNewline
}

$darkTheme = "src\app\themes\dark-theme.scss"
if (Test-Path $darkTheme) {
    Write-Host "  $darkTheme" -ForegroundColor Green
    $content = Get-Content $darkTheme -Raw
    $content = $content -replace "@import './variables';", "@use './variables' as *;"
    Set-Content $darkTheme $content -NoNewline
}

# Component files (shared) - Remove variables import, keep only mixins
Write-Host "`nUpdating shared components..." -ForegroundColor Yellow
$sharedComponents = @(
    "src\app\shared\components\sidebar\sidebar.component.scss",
    "src\app\shared\components\navbar\navbar.component.scss",
    "src\app\shared\components\notification-bell\notification-bell.component.scss",
    "src\app\shared\components\info-card\info-card.component.scss",
    "src\app\shared\components\transaction-item\transaction-item.component.scss",
    "src\app\shared\components\loader\loader.component.scss",
    "src\app\shared\components\custom-button\custom-button.component.scss",
    "src\app\shared\components\chart-widget\chart-widget.component.scss",
    "src\app\shared\components\page-header\page-header.component.scss"
)

foreach ($file in $sharedComponents) {
    if (Test-Path $file) {
        Write-Host "  $file" -ForegroundColor Green
        $content = Get-Content $file -Raw
        # Remove the variables import line completely
        $content = $content -replace "@import '../../../themes/variables';`r?`n", ""
        # Replace mixins import with @use
        $content = $content -replace "@import '../../../themes/mixins';", "@use '../../../themes/mixins' as *;"
        Set-Content $file $content -NoNewline
    }
}

# Layout files
Write-Host "`nUpdating layout files..." -ForegroundColor Yellow
$layouts = @(
    "src\app\layouts\main-layout\main-layout.component.scss",
    "src\app\layouts\auth-layout\auth-layout.component.scss"
)

foreach ($file in $layouts) {
    if (Test-Path $file) {
        Write-Host "  $file" -ForegroundColor Green
        $content = Get-Content $file -Raw
        $content = $content -replace "@import '../../themes/variables';`r?`n", ""
        $content = $content -replace "@import '../../themes/mixins';", "@use '../../themes/mixins' as *;"
        Set-Content $file $content -NoNewline
    }
}

# Page files
Write-Host "`nUpdating page files..." -ForegroundColor Yellow
$pages = @(
    "src\app\pages\dashboard\dashboard.component.scss",
    "src\app\pages\login\login.component.scss",
    "src\app\pages\mfa\mfa.component.scss",
    "src\app\pages\accounts\accounts.component.scss"
)

foreach ($file in $pages) {
    if (Test-Path $file) {
        Write-Host "  $file" -ForegroundColor Green
        $content = Get-Content $file -Raw
        $content = $content -replace "@import '../../themes/variables';`r?`n", ""
        $content = $content -replace "@import '../../themes/mixins';", "@use '../../themes/mixins' as *;"
        Set-Content $file $content -NoNewline
    }
}

Write-Host "`nAll SCSS files updated successfully!" -ForegroundColor Green
Write-Host "Changes made:" -ForegroundColor Cyan
Write-Host "  - Converted @import to @use in theme files" -ForegroundColor White
Write-Host "  - Removed duplicate variables imports" -ForegroundColor White
Write-Host "  - Kept only mixins import (which includes variables)" -ForegroundColor White
Write-Host "`nNow run: ng serve" -ForegroundColor Yellow
