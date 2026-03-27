"""
app.py  —  LinkPeek  (Vulnerable Demo App)
------------------------------------------
A realistic "URL Preview" web service.

VULNERABLE BEHAVIOR:
  The /fetch endpoint accepts a user-supplied URL and makes a
  server-side HTTP request to it — classic SSRF.

NAIVE PROTECTION (bypassable):
  The app tries to block obvious internal destinations by checking
  for the strings "localhost" and "127.0.0.1". This is insufficient.

BYPASS:
  Use  http://0.0.0.0:8080/api/config
  → "0.0.0.0" is not in the blocklist
  → On Linux, 0.0.0.0 routes to the loopback interface
  → The server reaches the internal service and returns its secrets
"""

from flask import Flask, request, jsonify, render_template, abort
import requests
import re
import os

app = Flask(__name__)

# ------------------------------------------------------------------ #
#  NAIVE blocklist — this is intentionally weak for the demo          #
# ------------------------------------------------------------------ #
BLOCKED_KEYWORDS = ["localhost", "127.0.0.1"]

def naive_check(url: str) -> tuple[bool, str]:
    """Returns (is_blocked, reason)."""
    lower = url.lower()
    for kw in BLOCKED_KEYWORDS:
        if kw in lower:
            return True, f"Blocked: URL contains disallowed keyword '{kw}'"
    return False, ""


# ------------------------------------------------------------------ #
#  Routes                                                              #
# ------------------------------------------------------------------ #

@app.route("/")
def index():
    return render_template("index.html")


@app.route("/fetch", methods=["POST"])
def fetch():
    """
    Core vulnerable endpoint.
    Accepts JSON: { "url": "https://..." }
    Returns the server's HTTP response back to the caller.
    """
    data = request.get_json(silent=True)
    if not data or "url" not in data:
        return jsonify({"error": "Missing 'url' in request body"}), 400

    target_url = data["url"].strip()

    # --- Naive protection (bypassable) ---
    blocked, reason = naive_check(target_url)
    if blocked:
        return jsonify({
            "error": reason,
            "hint": "Access to internal addresses is restricted."
        }), 403

    # --- Actually fetch the URL server-side ---
    try:
        resp = requests.get(
            target_url,
            timeout=5,
            allow_redirects=True,          # following redirects also enables bypasses
            headers={"User-Agent": "LinkPeek/1.0 Preview-Bot"}
        )
        content_type = resp.headers.get("Content-Type", "text/plain")

        # Try to parse as JSON for cleaner display
        try:
            body = resp.json()
            return jsonify({
                "status_code": resp.status_code,
                "content_type": content_type,
                "url_fetched": target_url,
                "response": body
            })
        except Exception:
            return jsonify({
                "status_code": resp.status_code,
                "content_type": content_type,
                "url_fetched": target_url,
                "response": resp.text[:4000]   # cap output length
            })

    except requests.exceptions.ConnectionError:
        return jsonify({"error": "Could not connect to the target URL."}), 502
    except requests.exceptions.Timeout:
        return jsonify({"error": "Request timed out."}), 504
    except Exception as e:
        return jsonify({"error": f"Unexpected error: {str(e)}"}), 500


@app.route("/api/status")
def status():
    """Public status endpoint — shows the app is alive."""
    return jsonify({
        "service": "LinkPeek",
        "version": "2.3.1",
        "status": "operational"
    })

if __name__ == "__main__":
    print("[LinkPeek] Starting on http://0.0.0.0:5000")
    app.run(host="0.0.0.0", port=5000, debug=False)
