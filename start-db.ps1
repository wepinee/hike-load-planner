# Опционально: MySQL через Docker (если не ставите MySQL на Windows)
Set-Location $PSScriptRoot

$docker = Get-Command docker -ErrorAction SilentlyContinue
if (-not $docker) {
    Write-Host "Docker не найден. Установите MySQL 8 локально — см. README.md" -ForegroundColor Yellow
    exit 1
}

Write-Host "Запуск MySQL в Docker..."
docker compose up -d

if ($LASTEXITCODE -ne 0) {
    Write-Host "Ошибка docker compose. Запущен ли Docker Desktop?" -ForegroundColor Red
    exit 1
}

Write-Host "MySQL: localhost:3306, БД hike_load, user hike / hike_secret" -ForegroundColor Green
Write-Host "Запуск приложения: mvn spring-boot:run"
