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

foreach ($f in $files) {
  if (Test-Path $f) {
    $content = [System.IO.File]::ReadAllText($f)
    $content = $content.TrimStart([char]0xFEFF)
    $utf8NoBom = New-Object System.Text.UTF8Encoding $false
    [System.IO.File]::WriteAllText($f, $content, $utf8NoBom)
    Write-Host "Removed BOM: $f"
  }
}

Write-Host "Done removing BOMs"
