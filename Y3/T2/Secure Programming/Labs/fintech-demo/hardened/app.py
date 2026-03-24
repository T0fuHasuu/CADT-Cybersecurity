"""
app.py - Flask application factory for the HARDENED demo app.

Security improvements over vulnerable version:
  • SECRET_KEY loaded from environment variable (never hardcoded)
  • Startup check enforces minimum key entropy (32 bytes / 256 bits)
  • SESSION_COOKIE_HTTPONLY: True  → JS cannot read the cookie
  • SESSION_COOKIE_SAMESITE: Strict → mitigates CSRF
  • PERMANENT_SESSION_LIFETIME: 30 min idle timeout
  • debug=False enforced (no stack traces exposed to clients)
  • X-Content-Type-Options, X-Frame-Options, Referrer-Policy headers added
"""

import os
import secrets
from flask import Flask, render_template, session
from datetime import timedelta

from auth      import auth_bp
from accounts  import accounts_bp
from transfers import transfers_bp
from admin     import admin_bp


def _validate_secret_key(key: str):
    """
    Refuse to start if SECRET_KEY is too short or is the demo default.
    A 256-bit (32 byte) random key provides adequate session security.
    """
    forbidden = {"supersecret123", "secret", "changeme", "dev", "test"}
    if key.lower() in forbidden or len(key) < 32:
        raise RuntimeError(
            "SECRET_KEY is insecure or too short. "
            "Generate one with: python -c \"import secrets; print(secrets.token_hex(32))\""
        )


def create_app() -> Flask:
    app = Flask(__name__, template_folder="templates")

    # ── SECRET_KEY from environment — never hardcoded ──────────────────────
    secret_key = os.environ.get("SECRET_KEY", "")
    _validate_secret_key(secret_key)
    app.config["SECRET_KEY"] = secret_key

    # ── Secure session settings ────────────────────────────────────────────
    app.config["SESSION_COOKIE_HTTPONLY"]    = True
    app.config["SESSION_COOKIE_SAMESITE"]    = "Strict"
    app.config["SESSION_COOKIE_SECURE"]      = os.environ.get("HTTPS", "0") == "1"
    app.config["PERMANENT_SESSION_LIFETIME"] = timedelta(minutes=30)
    app.config["SESSION_REFRESH_EACH_REQUEST"] = True

    # ── Register blueprints ────────────────────────────────────────────────
    app.register_blueprint(auth_bp)
    app.register_blueprint(accounts_bp)
    app.register_blueprint(transfers_bp)
    app.register_blueprint(admin_bp)

    # ── Security response headers ──────────────────────────────────────────
    @app.after_request
    def set_security_headers(response):
        response.headers["X-Content-Type-Options"] = "nosniff"
        response.headers["X-Frame-Options"]         = "DENY"
        response.headers["Referrer-Policy"]         = "strict-origin-when-cross-origin"
        response.headers["X-XSS-Protection"]        = "1; mode=block"
        return response

    # ── Session expiry check ───────────────────────────────────────────────
    @app.before_request
    def enforce_session_timeout():
        session.permanent = True   # enables PERMANENT_SESSION_LIFETIME

    # ── Error handlers ─────────────────────────────────────────────────────
    @app.errorhandler(403)
    def forbidden(e):
        return render_template("error.html", code=403,
                               message="Access denied."), 403

    @app.errorhandler(404)
    def not_found(e):
        return render_template("error.html", code=404,
                               message="Page not found."), 404

    @app.errorhandler(500)
    def server_error(e):
        # Return generic message — no stack trace
        return render_template("error.html", code=500,
                               message="Internal server error."), 500

    return app


if __name__ == "__main__":
    app = create_app()
    app.run(
        host="127.0.0.1",           # localhost only — not 0.0.0.0
        port=int(os.environ.get("PORT", 5001)),
        debug=False                 # debug=False — no stack traces
    )
