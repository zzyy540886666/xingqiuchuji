# 从 Windows 调用 WSL 执行 setup-and-run.sh（默认克隆到 WSL 的 $HOME/screenshot-to-code）
#
# 推荐：先在 WSL 里 export 密钥，再执行本脚本，避免 PowerShell 转义问题：
#   wsl
#   export OPENAI_API_KEY='sk-...'
#   exit
#   .\run-from-windows.ps1
#
#
$shWin = Join-Path $PSScriptRoot "setup-and-run.sh"
$shUnix = (wsl wslpath -a $shWin).Trim()
if (-not $shUnix) { throw "wslpath failed for: $shWin" }

# 将 Windows 环境变量传入 WSL（值里勿含单引号；含单请改为在 wsl 内手动 export）
$prefix = ""
if ($env:OPENAI_API_KEY -and $env:OPENAI_API_KEY -notmatch "'") {
  $prefix = "export OPENAI_API_KEY='$($env:OPENAI_API_KEY)'; "
}
if ($env:ANTHROPIC_API_KEY -and $env:ANTHROPIC_API_KEY -notmatch "'") {
  $prefix += "export ANTHROPIC_API_KEY='$($env:ANTHROPIC_API_KEY)'; "
}
if ($env:GEMINI_API_KEY -and $env:GEMINI_API_KEY -notmatch "'") {
  $prefix += "export GEMINI_API_KEY='$($env:GEMINI_API_KEY)'; "
}

wsl bash -lc "$prefix bash `"$shUnix`""
