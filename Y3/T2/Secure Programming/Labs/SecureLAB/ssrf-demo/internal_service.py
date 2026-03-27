"""
internal_service.py
-------------------
Simulates an internal-only service (admin API / config endpoint).
Bound ONLY to 127.0.0.1 — not reachable from outside the machine.
This is what the attacker wants to reach via SSRF.
"""

from flask import Flask, jsonify
import os
from dotenv import dotenv_values

app = Flask(__name__)

# Load .env for demo
config = dotenv_values(".env")

@app.route("/")
def index():
    return jsonify({
        "service": "Internal Admin API",
        "status": "running",
        "message": "This service is internal only. Not exposed to the internet.",
        "endpoints": [
            "/api/config",
            "/api/health",
            "/api/users"
        ]
    })

@app.route("/api/config")
def get_config():
    """Simulates leaking .env / config secrets via SSRF."""
    return jsonify({
        "warning": "INTERNAL USE ONLY",
        "environment": config.get("APP_ENV", "unknown"),
        "db_host": config.get("DB_HOST"),
        "db_user": config.get("DB_USER"),
        "db_pass": config.get("DB_PASS"),
        "internal_api_key": config.get("INTERNAL_API_KEY"),
        "admin_jwt_secret": config.get("ADMIN_JWT_SECRET"),
        "aws_access_key_id": config.get("AWS_ACCESS_KEY_ID"),
        "aws_secret_access_key": config.get("AWS_SECRET_ACCESS_KEY"),
        "smtp_pass": config.get("SMTP_PASS"),
    })

@app.route("/api/health")
def health():
    return jsonify({"status": "healthy", "uptime": "99.8%", "host": "10.0.0.1"})

@app.route("/api/users")
def users():
    return jsonify({
        "users": [
            {"id": 1, "username": "admin",   "role": "superadmin", "email": "admin@internal.corp"},
            {"id": 2, "username": "svc_bot", "role": "service",    "email": "bot@internal.corp"},
            {"id": 3, "username": "dbadmin", "role": "dba",        "email": "dba@internal.corp"},
        ]
    })

if __name__ == "__main__":
    # ONLY bind to localhost — not accessible from outside
    print("[Internal Service] Starting on http://127.0.0.1:8080 (internal only)")
    app.run(host="127.0.0.1", port=8080, debug=False)
