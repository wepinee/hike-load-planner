# Проверка REST API (этап 2): авторизация + поход + раскладка
# Запуск: .\test-api.ps1  (приложение должно работать на :8080)

[Console]::OutputEncoding = [System.Text.Encoding]::UTF8
$OutputEncoding = [System.Text.Encoding]::UTF8

$BaseUrl = "http://localhost:8080"
$Token = $null

function Invoke-Api($Method, $Uri, $Body = $null, [switch]$NoAuth) {
    $headers = @{}
    if (-not $NoAuth -and $Token) {
        $headers.Authorization = "Bearer $Token"
    }
    $params = @{
        Method      = $Method
        Uri         = "$BaseUrl$Uri"
        Headers     = $headers
        ErrorAction = "Stop"
    }
    if ($Body) {
        $json = $Body | ConvertTo-Json -Depth 5 -Compress
        $params.Body = [System.Text.Encoding]::UTF8.GetBytes($json)
        $params.ContentType = "application/json; charset=utf-8"
    }
    Write-Host "`n>>> $Method $Uri" -ForegroundColor Cyan
    $response = Invoke-RestMethod @params
    $response | ConvertTo-Json -Depth 8
    return $response
}

Write-Host "1. Login JWT (organizer@hike.local / demo)" -ForegroundColor Green
$auth = Invoke-Api POST "/api/auth/login" @{
    email    = "organizer@hike.local"
    password = "demo"
} -NoAuth
$Token = $auth.accessToken
Write-Host "Token: $($Token.Substring(0, [Math]::Min(40, $Token.Length)))..." -ForegroundColor DarkGray

Write-Host "2. Create hike" -ForegroundColor Green
$hike = Invoke-Api POST "/api/hikes" @{
    name         = "Testovyy pohod"
    startDate    = "2026-08-01"
    durationDays = 3
}
$hikeId = $hike.id

Write-Host "3. Add participant" -ForegroundColor Green
Invoke-Api POST "/api/hikes/$hikeId/participants" @{
    name        = "Anna"
    email       = "anna@test.local"
    gender      = "FEMALE"
    maxWeightKg = 15
}

Write-Host "4. Shared gear" -ForegroundColor Green
Invoke-Api POST "/api/hikes/$hikeId/gear/shared" @{
    name     = "Palatka"
    weightKg = 2.5
    type     = "SHARED"
}

Write-Host "5. Food" -ForegroundColor Green
Invoke-Api POST "/api/hikes/$hikeId/food" @{
    name                    = "Grechka"
    weightPerPortionKg      = 0.08
    caloriesPerPortion      = 320
    portionsPerPersonPerDay = 2
}

Write-Host "6. Generate load plan" -ForegroundColor Green
Invoke-Api POST "/api/hikes/$hikeId/load-plan/generate"

Write-Host "7. Export JSON" -ForegroundColor Green
Invoke-Api GET "/api/hikes/$hikeId/load-plan/export"

Write-Host "`nDone." -ForegroundColor Green
