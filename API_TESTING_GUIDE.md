# API Testing Guide - Custom Authentication

## Overview
This guide shows how to test all endpoints of the Custom Authentication API.

**Base URL:** `http://localhost:8085`

---

## 1. User Registration

**Endpoint:** `POST /auth/register`

**Request:**
```powershell
Invoke-WebRequest -Uri "http://localhost:8085/auth/register" `
    -Method POST `
    -ContentType "application/json" `
    -Body '{"email":"newuser@example.com","password":"password123"}'
```

**Expected Response (200 OK):**
```json
{
  "message": "Registration successful. Please check your email to verify your account.",
  "token": null,
  "email": null
}
```

**Check Application Logs for:**
```
Verification email sent to: newuser@example.com
Verification link: http://localhost:8085/auth/verify?token=<UUID>
```

---

## 2. Email Verification

**Endpoint:** `GET /auth/verify?token={token}`

Copy the token from the logs and use it:

### Test 2A: First Verification (Fresh Token)
```powershell
Invoke-WebRequest -Uri "http://localhost:8085/auth/verify?token=YOUR_TOKEN_HERE" -Method GET
```

**Expected Response (200 OK):**
```json
{
  "message": "Email verified successfully. You can now login.",
  "token": null,
  "email": "newuser@example.com"
}
```
✅ **Notice:** Email is now returned in response!

### Test 2B: Second Verification (Already Verified)
Click the same link again:

**Expected Response (200 OK):**
```json
{
  "message": "Your email is already verified. Please proceed to login.",
  "token": null,
  "email": "newuser@example.com"
}
```
✅ **Improvement:** No error! Just informative message.

### Test 2C: Invalid Token
```powershell
Invoke-WebRequest -Uri "http://localhost:8085/auth/verify?token=invalid-token-123" -Method GET
```

**Expected Response (400 Bad Request):**
```json
{
  "message": "Invalid or expired verification token. Please request a new one by attempting to login."
}
```
✅ **Improvement:** Clearer error message with next steps.

---

## 3. Login (Unverified User)

**Endpoint:** `POST /auth/login`

### Test 3A: Login Without Verification
Register a new user but don't verify:

```powershell
Invoke-WebRequest -Uri "http://localhost:8085/auth/login" `
    -Method POST `
    -ContentType "application/json" `
    -Body '{"email":"unverified@example.com","password":"password123"}'
```

**Expected Response (401 Unauthorized):**
```json
{
  "message": "Account not verified. A new verification email has been sent to your email address."
}
```

**Check Logs:** New verification token generated and email sent.

### Test 3B: Login Again Within 5 Minutes (Throttling Test)
Try to login again immediately:

**Expected Response (401 Unauthorized):**
```json
{
  "message": "Account not verified. Please wait 4 more minute(s) before requesting a new verification email."
}
```
✅ **Throttling working!**

---

## 4. Login (Verified User)

**Endpoint:** `POST /auth/login`

After verifying email:

```powershell
$response = Invoke-WebRequest -Uri "http://localhost:8085/auth/login" `
    -Method POST `
    -ContentType "application/json" `
    -Body '{"email":"newuser@example.com","password":"password123"}' `
    -UseBasicParsing

$response.Content | ConvertFrom-Json
```

**Expected Response (200 OK):**
```json
{
  "message": "Login successful",
  "token": "eyJhbGciOiJIUzI1NiJ9.eyJzdWIiOiJuZXd1c2VyQGV4YW1wbGUuY29tIiwiaWF0IjoxNzA5...",
  "email": "newuser@example.com"
}
```

**Save the JWT token for next step!**

---

## 5. Protected Endpoint Test

**Endpoint:** `GET /auth/test`

Use the JWT token from login response:

```powershell
$token = "YOUR_JWT_TOKEN_FROM_LOGIN"

Invoke-WebRequest -Uri "http://localhost:8085/auth/test" `
    -Method GET `
    -Headers @{"Authorization"="Bearer $token"} `
    -UseBasicParsing
```

**Expected Response (200 OK):**
```json
{
  "message": "This is a protected endpoint. You are authenticated!",
  "token": null,
  "email": null
}
```

### Test 5B: Access Without Token
```powershell
Invoke-WebRequest -Uri "http://localhost:8085/auth/test" -Method GET
```

**Expected Response (403 Forbidden):**
- Access denied

---

## 6. Swagger UI Testing

Open in browser:
```
http://localhost:8085/swagger-ui.html
```

You can test all endpoints interactively through Swagger UI!

---

## Complete Test Scenario

Run this complete flow:

```powershell
# 1. Register
Write-Host "`n=== STEP 1: Register User ===" -ForegroundColor Cyan
Invoke-WebRequest -Uri "http://localhost:8085/auth/register" `
    -Method POST -ContentType "application/json" `
    -Body '{"email":"demo@test.com","password":"secure123"}' `
    -UseBasicParsing

Write-Host "`nCheck logs for verification token...`n" -ForegroundColor Yellow
Read-Host "Copy token from logs and press Enter"

# 2. Verify
$token = Read-Host "Enter verification token"
Write-Host "`n=== STEP 2: Verify Email ===" -ForegroundColor Cyan
$verifyResponse = Invoke-WebRequest -Uri "http://localhost:8085/auth/verify?token=$token" `
    -Method GET -UseBasicParsing
$verifyResponse.Content | ConvertFrom-Json | Format-List

# 3. Login
Write-Host "`n=== STEP 3: Login ===" -ForegroundColor Cyan
$loginResponse = Invoke-WebRequest -Uri "http://localhost:8085/auth/login" `
    -Method POST -ContentType "application/json" `
    -Body '{"email":"demo@test.com","password":"secure123"}' `
    -UseBasicParsing

$loginData = $loginResponse.Content | ConvertFrom-Json
$loginData | Format-List

# 4. Test Protected Endpoint
Write-Host "`n=== STEP 4: Access Protected Endpoint ===" -ForegroundColor Cyan
Invoke-WebRequest -Uri "http://localhost:8085/auth/test" `
    -Method GET `
    -Headers @{"Authorization"="Bearer $($loginData.token)"} `
    -UseBasicParsing

Write-Host "`n=== ALL TESTS COMPLETE ===" -ForegroundColor Green
```

---

## Summary of Improvements

### ✅ What Changed:
1. **Verification Response Now Includes Email** - Confirms which account was verified
2. **Better Error Messages** - Clear next steps for users
3. **Already-Verified Handling** - No error if clicking link multiple times
4. **Improved Token Validation** - More helpful error messages

### ✅ What Still Works:
- JWT authentication
- 10-minute token expiry
- 5-minute email throttling
- One-time token use
- Old token invalidation
- PostgreSQL persistence

---

## Quick Reference

| Endpoint | Method | Auth Required | Purpose |
|----------|--------|---------------|---------|
| `/auth/register` | POST | No | Register new user |
| `/auth/verify` | GET | No | Verify email |
| `/auth/login` | POST | No | Get JWT token |
| `/auth/test` | GET | Yes (JWT) | Test authentication |
| `/swagger-ui.html` | GET | No | API documentation |
