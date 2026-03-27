#!/usr/bin/env bash
# ============================================================
#  setup.sh  —  SSRF Demo Environment Setup (Arch Linux)
# ============================================================

set -e

echo ""
echo "╔══════════════════════════════════════════════╗"
echo "║   SSRF Demo — Setup Script (Arch Linux)      ║"
echo "╚══════════════════════════════════════════════╝"
echo ""

# ── 1. Install Python if missing ───────────────────────────
if ! command -v python3 &>/dev/null; then
  echo "[*] Installing python3..."
  sudo pacman -S --noconfirm python python-pip
else
  echo "[✓] Python3 found: $(python3 --version)"
fi

# ── 2. Install pip packages ────────────────────────────────
echo "[*] Installing Python dependencies..."
pip install -r requirements.txt --break-system-packages -q
echo "[✓] Dependencies installed"

# ── 3. Verify .env exists ──────────────────────────────────
if [ ! -f .env ]; then
  echo "[!] .env file not found. Make sure it is in this directory."
  exit 1
fi
echo "[✓] .env file found"

echo ""
echo "Setup complete. Run the demo with:  bash run.sh"
echo ""
