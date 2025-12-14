# Test Gemini API Directly

Let's test if your new API key actually works with gemini-2.0-flash-exp:

## Quick PowerShell Test

```powershell
$apiKey = "AIzaSyB0qt8I9un9jLdBaotCsY2qZBr75LTWQvk"

$body = @{
    contents = @(
        @{
            parts = @(
                @{
                    text = "Say hello"
                }
            )
        }
    )
    tools = @(
        @{
            google_search = @{}
        }
    )
} | ConvertTo-Json -Depth 10

$response = Invoke-RestMethod `
    -Uri "https://generativelanguage.googleapis.com/v1beta/models/gemini-2.0-flash-exp:generateContent?key=$apiKey" `
    -Method POST `
    -ContentType "application/json" `
    -Body $body

$response | ConvertTo-Json -Depth 10
```

## What This Tests:

1. Your new API key validity
2. Access to gemini-2.0-flash-exp model
3. google_search tool support

## Expected Output:

Should return a JSON response with "Hello" message.

## If It Fails:

Copy the error message - it will tell us exactly what's wrong:

- 404 = Model not available
- 403 = API key issue
- 400 = Syntax error

---

**Run this in PowerShell and tell me what happens!**
