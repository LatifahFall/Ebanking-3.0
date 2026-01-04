$files = @(
  'src\test\java\com\ebanking\payment\kafka\PaymentEventProducerTest.java',
  'src\test\java\com\ebanking\payment\service\PaymentServiceTest.java',
  'src\test\java\com\ebanking\payment\exception\GlobalExceptionHandlerTest.java',
  'src\test\java\com\ebanking\payment\service\QrCodeServiceTest.java',
  'src\test\java\com\ebanking\payment\repository\PaymentRepositoryTest.java',
  'src\test\java\com\ebanking\payment\client\AccountServiceClientTest.java',
  'src\test\java\com\ebanking\payment\kafka\AccountEventConsumerTest.java',
  'src\test\java\com\ebanking\payment\controller\PaymentControllerTest.java'
)

$counter = 1

foreach ($file in $files) {
  if (Test-Path $file) {
    Write-Host "Processing: $file"
    
    $content = Get-Content $file -Raw -Encoding UTF8
    
    # Replace UUID.randomUUID() with sequential Longs
    while ($content -match 'UUID\.randomUUID\(\)') {
      $content = $content -replace 'UUID\.randomUUID\(\)', "${counter}L"
      $counter++
    }
    
    # Replace UUID type with Long for paymentId
    $content = $content -replace '\bUUID\s+paymentId\b', 'Long paymentId'
    
    # Remove UUID import if no other UUID references
    if (-not ($content -match '\bUUID\b')) {
      $content = $content -replace 'import java\.util\.UUID;\r?\n', ''
    }
    
    Set-Content -Path $file -Value $content -Encoding UTF8 -NoNewline
    Write-Host "Fixed"
  }
}

Write-Host "Done! Fixed UUID references"
