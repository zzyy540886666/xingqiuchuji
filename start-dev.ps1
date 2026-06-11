# ============================================================
# XingQiu Dev Launcher - Backend + Admin Web
# Usage: Right-click -> Run with PowerShell
# ============================================================

$ErrorActionPreference = "Stop"
$Host.UI.RawUI.WindowTitle = "XingQiu - Starting..."

$PROJECT_ROOT = Split-Path -Parent $MyInvocation.MyCommand.Path
$BACKEND_DIR  = Join-Path $PROJECT_ROOT "backend"
$ADMIN_DIR    = Join-Path $PROJECT_ROOT "admin-web"
$BACKEND_PORT = 8080
$ADMIN_PORT   = 3000
$ENV_FILE     = Join-Path $PROJECT_ROOT ".env"

if (Test-Path $ENV_FILE) {
    Get-Content $ENV_FILE | ForEach-Object {
        $line = $_.Trim()
        if (-not $line -or $line.StartsWith("#") -or -not $line.Contains("=")) { return }
        $parts = $line.Split("=", 2)
        $name = $parts[0].Trim()
        $value = $parts[1].Trim().Trim('"').Trim("'")
        if ($name) {
            [Environment]::SetEnvironmentVariable($name, $value, "Process")
        }
    }
}

# Local development always runs the backend on the host machine and only uses
# Docker for MySQL/Redis. Do not inherit production-style .env hosts/passwords.
$env:DB_USERNAME = "root"
$env:DB_PASSWORD = "xingqiu123"
$env:DB_URL = "jdbc:mysql://localhost:3306/xingqiu_dev?useUnicode=true&characterEncoding=UTF-8&connectionCollation=utf8mb4_unicode_ci&serverTimezone=Asia/Shanghai"
$env:REDIS_HOST = "localhost"
$env:REDIS_PASSWORD = "redis123"
$env:SPRING_DATASOURCE_URL = $env:DB_URL
$env:SPRING_DATASOURCE_USERNAME = $env:DB_USERNAME
$env:SPRING_DATASOURCE_PASSWORD = $env:DB_PASSWORD
$env:SPRING_DATA_REDIS_HOST = $env:REDIS_HOST
$env:SPRING_DATA_REDIS_PASSWORD = $env:REDIS_PASSWORD
if (-not $env:JWT_SECRET)     { $env:JWT_SECRET     = "dev-secret-change-me" }

# Force dev profile for local development (ignore .env production setting)
$env:SPRING_PROFILES_ACTIVE = "dev"

# --- Helper Functions ---

function Write-Banner($text) {
    $line = "=" * 60
    Write-Host ""
    Write-Host $line -ForegroundColor Cyan
    Write-Host "  $text" -ForegroundColor Cyan
    Write-Host $line -ForegroundColor Cyan
    Write-Host ""
}

function Write-Step($step, $text) {
    Write-Host "[$step] " -ForegroundColor Yellow -NoNewline
    Write-Host $text
}

function Write-Ok($text) {
    Write-Host "  [OK] " -ForegroundColor Green -NoNewline
    Write-Host $text
}

function Write-Fail($text) {
    Write-Host "  [FAIL] " -ForegroundColor Red -NoNewline
    Write-Host $text
}

function Get-ListeningPortProcessIds($port) {
    $foundPids = @()

    try {
        $conns = Get-NetTCPConnection -LocalPort $port -State Listen -ErrorAction Stop
        foreach ($c in $conns) {
            if ($c.OwningProcess -and $c.OwningProcess -gt 0) {
                $foundPids += [int]$c.OwningProcess
            }
        }
    }
    catch { }

    if (-not $foundPids) {
        $pattern = ":$port$"
        $rows = netstat -ano -p tcp 2>$null | Select-String "LISTENING"
        foreach ($row in $rows) {
            $cols = $row.ToString().Trim() -split "\s+"
            if ($cols.Length -ge 5 -and $cols[1] -match $pattern) {
                $parsedPid = 0
                if ([int]::TryParse($cols[-1], [ref]$parsedPid) -and $parsedPid -gt 0) {
                    $foundPids += $parsedPid
                }
            }
        }
    }

    return @($foundPids | Sort-Object -Unique)
}

function Kill-PortProcess($port) {
    $listeningPids = @(Get-ListeningPortProcessIds $port)
    if (-not $listeningPids) {
        Write-Ok "Port $port available"
        return $true
    }

    foreach ($processId in $listeningPids) {
        $p = Get-Process -Id $processId -ErrorAction SilentlyContinue
        if ($p -and $p.Name -ne "System" -and $p.Name -ne "Idle") {
            Write-Host "  Port $port occupied: PID=$($p.Id) ($($p.Name)), killing process tree..." -ForegroundColor DarkYellow
            Stop-ProcessTree $p.Id
        }
        else {
            Write-Fail "Port $port is occupied by PID=$processId and cannot be stopped automatically"
        }
    }

    for ($i = 0; $i -lt 20; $i++) {
        Start-Sleep -Milliseconds 500
        if (-not (Test-PortListening $port)) {
            Write-Ok "Port $port freed"
            return $true
        }
    }

    $remainingPids = @(Get-ListeningPortProcessIds $port)
    Write-Fail "Port $port is still occupied after cleanup: PID(s) $($remainingPids -join ', ')"
    return $false
}

function Wait-ForHttp($url, $timeoutSec) {
    $elapsed = 0
    while ($elapsed -lt $timeoutSec) {
        try {
            $r = Invoke-WebRequest -Uri $url -UseBasicParsing -TimeoutSec 5 -ErrorAction Stop
            if ($r.StatusCode -eq 200) { return $true }
        }
        catch { }
        Write-Host "." -NoNewline -ForegroundColor DarkGray
        Start-Sleep -Seconds 3
        $elapsed += 3
    }
    Write-Host ""
    return $false
}

function Wait-ForPort($port, $timeoutSec) {
    $elapsed = 0
    while ($elapsed -lt $timeoutSec) {
        if (Test-PortListening $port) { return $true }
        Write-Host "." -NoNewline -ForegroundColor DarkGray
        Start-Sleep -Seconds 2
        $elapsed += 2
    }
    Write-Host ""
    return $false
}

function Test-PortListening($port) {
    return [bool]@(Get-ListeningPortProcessIds $port)
}

function Test-DockerCommand {
    $ErrorActionPreference = "Continue"
    $version = docker version --format "{{.Server.Version}}" 2>&1
    $dockerExitCode = $LASTEXITCODE
    $ErrorActionPreference = "Stop"

    if ($dockerExitCode -ne 0) {
        Write-Fail "Docker is not available: $version"
        return $false
    }

    return $true
}

function Test-MySqlLogin {
    $ErrorActionPreference = "Continue"
    $result = docker exec xq-mysql mysqladmin ping "-u$env:DB_USERNAME" "-p$env:DB_PASSWORD" --silent 2>&1
    $mysqlExitCode = $LASTEXITCODE
    $ErrorActionPreference = "Stop"

    if ($mysqlExitCode -ne 0) {
        Write-Fail "Cannot connect to xq-mysql with $env:DB_USERNAME/$env:DB_PASSWORD"
        Write-Host "  MySQL said: $result" -ForegroundColor DarkYellow
        Write-Host "  If this container was initialized with another password, either set it back or recreate the dev volume:" -ForegroundColor DarkYellow
        Write-Host "    docker compose -p xingqiu down -v" -ForegroundColor DarkYellow
        Write-Host "    docker compose -p xingqiu up -d mysql redis" -ForegroundColor DarkYellow
        Write-Host "  Warning: down -v deletes the local dev database volume." -ForegroundColor DarkYellow
        return $false
    }

    Write-Ok "MySQL login verified"
    return $true
}

function Test-RedisLogin {
    $ErrorActionPreference = "Continue"
    $result = docker exec xq-redis redis-cli -a $env:REDIS_PASSWORD ping 2>&1
    $redisExitCode = $LASTEXITCODE
    $ErrorActionPreference = "Stop"

    if ($redisExitCode -ne 0 -or ($result -join "`n") -notmatch "PONG") {
        Write-Fail "Cannot connect to xq-redis with configured password"
        Write-Host "  Redis said: $result" -ForegroundColor DarkYellow
        return $false
    }

    Write-Ok "Redis login verified"
    return $true
}

function Stop-ProcessTree($processId) {
    if (-not $processId) { return }

    if (Get-Command taskkill.exe -ErrorAction SilentlyContinue) {
        & taskkill.exe /PID $processId /T /F 2>$null | Out-Null
        Start-Sleep -Milliseconds 300
        if (-not (Get-Process -Id $processId -ErrorAction SilentlyContinue)) {
            return
        }
    }

    $children = Get-CimInstance Win32_Process -ErrorAction SilentlyContinue |
        Where-Object { $_.ParentProcessId -eq $processId }
    foreach ($child in $children) {
        Stop-ProcessTree $child.ProcessId
    }
    Stop-Process -Id $processId -Force -ErrorAction SilentlyContinue
}

# ============================================================
# MAIN
# ============================================================

Write-Banner "XingQiu Dev Environment Launcher"

# --- 1. Docker ---
Write-Step "1/5" "Checking Docker services (MySQL / Redis)..."

if (-not (Test-DockerCommand)) {
    Write-Host "Press any key to exit..." -ForegroundColor DarkGray
    $null = $Host.UI.RawUI.ReadKey("NoEcho,IncludeKeyDown")
    exit 1
}

$ErrorActionPreference = "Continue"
$mysqlUp = docker ps --filter "name=xq-mysql" --filter "status=running" -q 2>&1
$redisUp = docker ps --filter "name=xq-redis" --filter "status=running" -q 2>&1
$ErrorActionPreference = "Stop"

if (-not $mysqlUp -or -not $redisUp) {
    Write-Host "  Starting Docker services..." -ForegroundColor DarkYellow
    Push-Location $PROJECT_ROOT
    $ErrorActionPreference = "Continue"
    docker compose -p xingqiu up -d mysql redis 2>&1 | Out-Null
    $ErrorActionPreference = "Stop"
    Pop-Location
    Start-Sleep -Seconds 5
}

if (-not (Test-MySqlLogin)) {
    Write-Host "Press any key to exit..." -ForegroundColor DarkGray
    $null = $Host.UI.RawUI.ReadKey("NoEcho,IncludeKeyDown")
    exit 1
}
if (-not (Test-RedisLogin)) {
    Write-Host "Press any key to exit..." -ForegroundColor DarkGray
    $null = $Host.UI.RawUI.ReadKey("NoEcho,IncludeKeyDown")
    exit 1
}

$ErrorActionPreference = "Continue"
$dockerBe = docker ps --filter "name=xq-backend" --filter "status=running" -q 2>&1
$dockerAdmin = docker ps --filter "name=xq-admin-web" --filter "status=running" -q 2>&1
$ErrorActionPreference = "Stop"
if ($dockerBe -and $dockerBe -notmatch "Error") {
    Write-Host "  Stopping Docker backend container (using local instead)..." -ForegroundColor DarkYellow
    $ErrorActionPreference = "Continue"
    docker stop xq-backend 2>&1 | Out-Null
    $ErrorActionPreference = "Stop"
}
if ($dockerAdmin -and $dockerAdmin -notmatch "Error") {
    Write-Host "  Stopping Docker admin-web container (using local instead)..." -ForegroundColor DarkYellow
    $ErrorActionPreference = "Continue"
    docker stop xq-admin-web 2>&1 | Out-Null
    $ErrorActionPreference = "Stop"
}

Write-Ok "MySQL and Redis ready"

# --- 2. Kill ports ---
Write-Step "2/5" "Checking ports..."
if (-not (Kill-PortProcess $BACKEND_PORT)) {
    Write-Fail "Cannot start backend until port $BACKEND_PORT is free"
    Write-Host "Press any key to exit..." -ForegroundColor DarkGray
    $null = $Host.UI.RawUI.ReadKey("NoEcho,IncludeKeyDown")
    exit 1
}
if (-not (Kill-PortProcess $ADMIN_PORT)) {
    Write-Fail "Cannot start admin-web until port $ADMIN_PORT is free"
    Write-Host "Press any key to exit..." -ForegroundColor DarkGray
    $null = $Host.UI.RawUI.ReadKey("NoEcho,IncludeKeyDown")
    exit 1
}

# --- 3. Start backend ---
Write-Step "3/5" "Starting backend (Spring Boot :$BACKEND_PORT)..."

$backendProc = Start-Process -FilePath "cmd.exe" `
    -ArgumentList "/k title [XingQiu Backend] & cd /d `"$BACKEND_DIR`" & mvn spring-boot:run" `
    -PassThru

Write-Host "  Waiting for backend (PID: $($backendProc.Id))..." -NoNewline
$backendReady = Wait-ForHttp "http://localhost:$BACKEND_PORT/meta/ping" 120

if ($backendReady) {
    Write-Ok "Backend started! (http://localhost:$BACKEND_PORT)"
}
else {
    Write-Fail "Backend startup timed out (120s). Check the [XingQiu Backend] window for errors."
    Write-Host "Press any key to exit..." -ForegroundColor DarkGray
    $null = $Host.UI.RawUI.ReadKey("NoEcho,IncludeKeyDown")
    exit 1
}

# --- 4. Start admin-web ---
Write-Step "4/5" "Starting admin-web (Vite :$ADMIN_PORT)..."

if (-not (Test-Path (Join-Path $ADMIN_DIR "node_modules"))) {
    Write-Host "  Installing dependencies (first run)..." -ForegroundColor DarkYellow
    Push-Location $ADMIN_DIR
    npm install --prefer-offline --no-audit --no-fund --registry=https://registry.npmmirror.com 2>$null | Out-Null
    Pop-Location
}

$adminProc = Start-Process -FilePath "cmd.exe" `
    -ArgumentList "/k title [XingQiu Admin-Web] & cd /d `"$ADMIN_DIR`" & npm run dev" `
    -PassThru

Write-Host "  Waiting for admin-web (PID: $($adminProc.Id))..." -NoNewline
$adminReady = Wait-ForPort $ADMIN_PORT 30

if ($adminReady) {
    Write-Ok "Admin-web started! (http://localhost:$ADMIN_PORT)"
}
else {
    Write-Fail "Admin-web startup timed out. Check the [XingQiu Admin-Web] window."
}

# --- 5. Done ---
Write-Step "5/5" "All services running!"
Write-Host ""
Write-Banner "READY - All Services Running"

Write-Host "  Backend API:     " -NoNewline
Write-Host "http://localhost:$BACKEND_PORT" -ForegroundColor Green
Write-Host "  Admin Panel:     " -NoNewline
Write-Host "http://localhost:$ADMIN_PORT" -ForegroundColor Green
Write-Host "  Admin Login:     " -NoNewline
Write-Host "admin / admin123" -ForegroundColor Green
Write-Host ""
Write-Host "  Admin Modules:" -ForegroundColor DarkGray
Write-Host "    Dashboard, Catalog, Operations, Moderation," -ForegroundColor DarkGray
Write-Host "    Orders, Users, Finance, Repair, Analytics, System" -ForegroundColor DarkGray
Write-Host ""
Write-Host "  API Endpoints:" -ForegroundColor DarkGray
Write-Host "    SKUs:    http://localhost:$BACKEND_PORT/api/v1/catalog/skus" -ForegroundColor DarkGray
Write-Host "    Scenes:  http://localhost:$BACKEND_PORT/api/v1/catalog/scenes" -ForegroundColor DarkGray
Write-Host "    Config:  http://localhost:$BACKEND_PORT/api/v1/config/app" -ForegroundColor DarkGray
Write-Host ""

$Host.UI.RawUI.WindowTitle = "XingQiu - Running"

Write-Host "============================================================" -ForegroundColor DarkCyan
Write-Host "  Press Ctrl+C or close this window to stop all services" -ForegroundColor DarkCyan
Write-Host "============================================================" -ForegroundColor DarkCyan
Write-Host ""

try {
    while ($true) {
        Start-Sleep -Seconds 5
        if (-not (Test-PortListening $BACKEND_PORT)) {
            $exitCode = if ($backendProc.HasExited) { $backendProc.ExitCode } else { "unknown" }
            Write-Fail "Backend port $BACKEND_PORT is no longer listening (process code: $exitCode)"
            break
        }
        if (-not (Test-PortListening $ADMIN_PORT)) {
            $exitCode = if ($adminProc.HasExited) { $adminProc.ExitCode } else { "unknown" }
            Write-Fail "Admin-web port $ADMIN_PORT is no longer listening (process code: $exitCode)"
            break
        }
    }
}
finally {
    Write-Host ""
    Write-Host "Stopping services..." -ForegroundColor Yellow
    Stop-ProcessTree $backendProc.Id
    Stop-ProcessTree $adminProc.Id
    Write-Host "All stopped." -ForegroundColor Green
}
