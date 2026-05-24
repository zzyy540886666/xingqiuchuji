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

$env:DB_USERNAME    = "root"
$env:DB_PASSWORD    = "xingqiu123"
$env:REDIS_HOST     = "localhost"
$env:REDIS_PASSWORD = "redis123"
$env:JWT_SECRET     = "xingqiu-jwt-secret-key-2026-0516"

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

function Kill-PortProcess($port) {
    $conns = Get-NetTCPConnection -LocalPort $port -State Listen -ErrorAction SilentlyContinue
    if ($conns) {
        foreach ($c in $conns) {
            $p = Get-Process -Id $c.OwningProcess -ErrorAction SilentlyContinue
            if ($p -and $p.Name -ne "System") {
                Write-Host "  Port $port occupied: PID=$($p.Id) ($($p.Name)), killing..." -ForegroundColor DarkYellow
                Stop-Process -Id $p.Id -Force -ErrorAction SilentlyContinue
                Start-Sleep -Milliseconds 500
            }
        }
        Write-Ok "Port $port freed"
    }
    else {
        Write-Ok "Port $port available"
    }
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
        $c = Get-NetTCPConnection -LocalPort $port -State Listen -ErrorAction SilentlyContinue
        if ($c) { return $true }
        Write-Host "." -NoNewline -ForegroundColor DarkGray
        Start-Sleep -Seconds 2
        $elapsed += 2
    }
    Write-Host ""
    return $false
}

# ============================================================
# MAIN
# ============================================================

Write-Banner "XingQiu Dev Environment Launcher"

# --- 1. Docker ---
Write-Step "1/5" "Checking Docker services (MySQL / Redis)..."

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
Kill-PortProcess $BACKEND_PORT
Kill-PortProcess $ADMIN_PORT

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
        if ($backendProc.HasExited) {
            Write-Fail "Backend process exited (code: $($backendProc.ExitCode))"
            break
        }
        if ($adminProc.HasExited) {
            Write-Fail "Admin-web process exited (code: $($adminProc.ExitCode))"
            break
        }
    }
}
finally {
    Write-Host ""
    Write-Host "Stopping services..." -ForegroundColor Yellow
    if (-not $backendProc.HasExited) {
        Stop-Process -Id $backendProc.Id -Force -ErrorAction SilentlyContinue
    }
    if (-not $adminProc.HasExited) {
        Stop-Process -Id $adminProc.Id -Force -ErrorAction SilentlyContinue
    }
    Get-Process -Name "java" -ErrorAction SilentlyContinue | Where-Object { $_.StartTime -gt (Get-Date).AddMinutes(-120) } | Stop-Process -Force -ErrorAction SilentlyContinue
    Get-Process -Name "node" -ErrorAction SilentlyContinue | Where-Object { $_.StartTime -gt (Get-Date).AddMinutes(-120) } | Stop-Process -Force -ErrorAction SilentlyContinue
    Write-Host "All stopped." -ForegroundColor Green
}
