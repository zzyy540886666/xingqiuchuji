#!/usr/bin/env bash
# screenshot-to-code：在 WSL 家目录下克隆/更新仓库，写入 .env，并 docker compose 启动。
# 用法：
#   export OPENAI_API_KEY='sk-...'   # 或 ANTHROPIC_API_KEY / GEMINI_API_KEY 至少一种
#   bash setup-and-run.sh
# 可选：SCREENSHOT_TO_CODE_DIR=~/code/screenshot-to-code bash setup-and-run.sh

set -euo pipefail

REPO_URL="${SCREENSHOT_TO_CODE_REPO:-https://github.com/abi/screenshot-to-code.git}"
TARGET="${SCREENSHOT_TO_CODE_DIR:-$HOME/screenshot-to-code}"

echo "[1/4] 目标目录: $TARGET"

if ! command -v docker >/dev/null 2>&1; then
  echo "错误: 未找到 docker。请在 Windows 安装 Docker Desktop 并启用 WSL 集成。" >&2
  exit 1
fi

if ! docker info >/dev/null 2>&1; then
  echo "错误: Docker 未运行或无权限。请启动 Docker Desktop。" >&2
  exit 1
fi

mkdir -p "$(dirname "$TARGET")"

if [[ ! -d "$TARGET/.git" ]]; then
  echo "[2/4] 克隆仓库..."
  git clone --depth 1 "$REPO_URL" "$TARGET"
else
  echo "[2/4] 已存在仓库，执行 git pull..."
  git -C "$TARGET" pull --ff-only || true
fi

cd "$TARGET"

echo "[3/4] 写入 .env..."
write_env() {
  local f="$TARGET/.env"
  : >"$f"
  if [[ -n "${OPENAI_API_KEY:-}" ]]; then
    echo "OPENAI_API_KEY=$OPENAI_API_KEY" >>"$f"
  fi
  if [[ -n "${ANTHROPIC_API_KEY:-}" ]]; then
    echo "ANTHROPIC_API_KEY=$ANTHROPIC_API_KEY" >>"$f"
  fi
  if [[ -n "${GEMINI_API_KEY:-}" ]]; then
    echo "GEMINI_API_KEY=$GEMINI_API_KEY" >>"$f"
  fi
  if [[ -n "${OPENAI_BASE_URL:-}" ]]; then
    echo "OPENAI_BASE_URL=$OPENAI_BASE_URL" >>"$f"
  fi
}

if [[ -n "${OPENAI_API_KEY:-}" || -n "${ANTHROPIC_API_KEY:-}" || -n "${GEMINI_API_KEY:-}" ]]; then
  write_env
  echo "已从环境变量写入密钥（.env 勿提交到 Git）。"
else
  if [[ ! -f .env ]]; then
    write_env
    {
      echo "OPENAI_API_KEY=REPLACE_ME"
      echo "# 可在下方追加："
      echo "# ANTHROPIC_API_KEY="
      echo "# GEMINI_API_KEY="
      echo "# OPENAI_BASE_URL=https://your-proxy/v1"
    } >>"$TARGET/.env"
    echo "警告: 未设置任何 API Key。已生成占位 .env，请编辑："
    echo "  $TARGET/.env"
    echo "保存后执行: cd \"$TARGET\" && docker compose up -d --build"
  else
    echo "保留已有 .env。"
  fi
fi

echo "[4/4] docker compose up -d --build ..."
docker compose up -d --build

echo ""
echo "完成。前端: http://localhost:5173"
echo "后端默认端口: http://localhost:7001"
echo "查看日志: cd \"$TARGET\" && docker compose logs -f"
echo "停止: cd \"$TARGET\" && docker compose down"
