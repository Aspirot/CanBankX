Set-StrictMode -Version Latest
$ErrorActionPreference = "Stop"

$root = Split-Path -Parent $MyInvocation.MyCommand.Path

$composeFiles = @(
    "docker-compose.yml",
    "client-service/docker-compose.yml",
    "account-service/docker-compose.yml",
    "ledger-service/docker-compose.yml",
    "payment-service/docker-compose.yml"
)

foreach ($file in $composeFiles) {
    Write-Host "Starting: $file" -ForegroundColor Cyan
    docker compose -f (Join-Path $root $file) up -d
}

Write-Host "All stacks started." -ForegroundColor Green
