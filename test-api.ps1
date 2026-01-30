# Custom Authentication API Testing Script

Write-Host "`n=== Testing Custom Authentication API ===" -ForegroundColor Cyan

# Test 1: Register a new user
Write-Host "`n[TEST 1] Registering new user..." -ForegroundColor Yellow
try {
    $response = Invoke-WebRequest -Uri "http://localhost:9090/auth/register" `
        -Method POST `
        -ContentType "application/json" `
        -Body '{"email":"demo@test.com","password":"secure123"}' `
        -UseBasicParsing
    
    $content = $response.Content | ConvertFrom-Json
    Write-Host "✓ Registration successful!" -ForegroundColor Green
    Write-Host "  Response: $($content.message)" -ForegroundColor Gray
} catch {
    Write-Host "✗ Registration failed: $($_.Exception.Message)" -ForegroundColor Red
}

# Wait for logs
Start-Sleep -Seconds 2

# Test 2: Try to login without verification
Write-Host "`n[TEST 2] Attempting login without verification..." -ForegroundColor Yellow
try {
    $response = Invoke-WebRequest -Uri "http://localhost:9090/auth/login" `
        -Method POST `
        -ContentType "application/json" `
        -Body '{"email":"demo@test.com","password":"secure123"}' `
        -UseBasicParsing
    
    Write-Host "✗ Should have failed!" -ForegroundColor Red
} catch {
    $errorResponse = $_.ErrorDetails.Message | ConvertFrom-Json
    Write-Host "✓ Login blocked as expected" -ForegroundColor Green
    Write-Host "  Response: $($errorResponse.message)" -ForegroundColor Gray
}

Write-Host "`n=== Check Application Logs for Verification Link ===" -ForegroundColor Cyan
Write-Host "Look for: 'Verification link: http://localhost:9090/auth/verify?token=...'" -ForegroundColor Yellow
Write-Host "`nCopy the token from logs and run:" -ForegroundColor Yellow
Write-Host '  Invoke-WebRequest -Uri "http://localhost:9090/auth/verify?token=YOUR_TOKEN" -Method GET' -ForegroundColor Gray

Write-Host "`nThen test login again with:" -ForegroundColor Yellow
Write-Host '  Invoke-WebRequest -Uri "http://localhost:9090/auth/login" -Method POST -ContentType "application/json" -Body ''{"email":"demo@test.com","password":"secure123"}''' -ForegroundColor Gray

Write-Host "`n=== Test Complete ===" -ForegroundColor Cyan
