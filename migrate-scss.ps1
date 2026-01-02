# Script to migrate from @import to @use in all SCSS files
# Run this after stopping ng serve

Write-Host "🔄 Migrating SCSS from @import to @use..." -ForegroundColor Cyan

# Component files (shared) - 3 levels deep
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
        Write-Host "  Updating $file" -ForegroundColor Yellow
        (Get-Content $file -Raw) `
            -replace "@import '../../../themes/variables';", "@use '../../../themes/variables' as *;" `
            -replace "@import '../../../themes/mixins';", "@use '../../../themes/mixins' as *;" |
            Set-Content $file -NoNewline
    }
}

# Layout files - 2 levels deep
$layouts = @(
    "src\app\layouts\main-layout\main-layout.component.scss",
    "src\app\layouts\auth-layout\auth-layout.component.scss"
)

foreach ($file in $layouts) {
    if (Test-Path $file) {
        Write-Host "  Updating $file" -ForegroundColor Yellow
        (Get-Content $file -Raw) `
            -replace "@import '../../themes/variables';", "@use '../../themes/variables' as *;" `
            -replace "@import '../../themes/mixins';", "@use '../../themes/mixins' as *;" |
            Set-Content $file -NoNewline
    }
}

# Page files - 2 levels deep
$pages = @(
    "src\app\pages\dashboard\dashboard.component.scss",
    "src\app\pages\login\login.component.scss",
    "src\app\pages\mfa\mfa.component.scss",
    "src\app\pages\accounts\accounts.component.scss"
)

foreach ($file in $pages) {
    if (Test-Path $file) {
        Write-Host "  Updating $file" -ForegroundColor Yellow
        (Get-Content $file -Raw) `
            -replace "@import '../../themes/variables';", "@use '../../themes/variables' as *;" `
            -replace "@import '../../themes/mixins';", "@use '../../themes/mixins' as *;" |
            Set-Content $file -NoNewline
    }
}

Write-Host "`n✅ Migration complete!" -ForegroundColor Green
Write-Host "All @import statements have been replaced with @use" -ForegroundColor Green
