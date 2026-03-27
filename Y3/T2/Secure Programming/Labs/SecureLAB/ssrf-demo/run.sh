#!/usr/bin/env bash
# ============================================================
#  run.sh  —  Start both services for the SSRF demo
# ============================================================

echo ""
echo "╔══════════════════════════════════════════════╗"
echo "║   SSRF Demo — Starting Services              ║"
echo "╚══════════════════════════════════════════════╝"
echo ""

# Kill any old instances
pkill -f "internal_service.py" 2>/dev/null || true
pkill -f "app.py" 2>/dev/null || true
sleep 1

# Start the internal service (background, only on localhost)
echo "[*] Starting internal service on http://127.0.0.1:8080 ..."
python3 internal_service.py &
INTERNAL_PID=$!
sleep 1
echo "[✓] Internal service running (PID $INTERNAL_PID)"

# Start the main app (foreground, accessible from network)
echo "[*] Starting LinkPeek web app on http://0.0.0.0:5000 ..."
echo ""
echo "┌─────────────────────────────────────────────┐"
echo "│  Open in your browser:                      │"
echo "│  http://<VM-IP>:5000                        │"
echo "└─────────────────────────────────────────────┘"
echo ""

# Trap Ctrl+C to kill both
trap "echo ''; echo 'Stopping...'; kill $INTERNAL_PID 2>/dev/null; exit 0" INT

python3 app.py
