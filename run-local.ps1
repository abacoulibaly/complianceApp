$ErrorActionPreference = "Stop"
$root = Split-Path -Parent $MyInvocation.MyCommand.Path
$envFile = Join-Path $root ".env"

if (-not (Test-Path $envFile)) {
    Write-Error ".env file not found. Copy .env.example to .env and set OPENAI_API_KEY."
}

Get-Content $envFile | ForEach-Object {
    $line = $_.Trim()
    if ($line -match '^\s*([^#=\s]+)\s*=\s*(.*)$') {
        $name = $matches[1].Trim()
        $value = $matches[2].Trim().Trim('"').Trim("'")
        Set-Item -Path "Env:$name" -Value $value
    }
}

$jar = Join-Path $root "target\compliance-automation-1.0.0-SNAPSHOT.jar"
if (-not (Test-Path $jar)) {
    Write-Host "Building application..."
    Push-Location $root
    mvn -q -DskipTests package
    Pop-Location
}

Write-Host "Starting compliance-automation on http://localhost:8080"
if (-not $env:OPENAI_API_KEY) {
    Write-Warning "OPENAI_API_KEY is not set. Vision extraction will be disabled."
} else {
    Write-Host "OpenAI API key loaded from .env"
}
java -jar $jar
