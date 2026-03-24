"""
app.py - Flask application factory for the VULNERABLE demo app.

FOR EDUCATIONAL USE ONLY.
Do not deploy this application to any network or production environment.

Note: SECRET_KEY is hardcoded here as a demo vulnerability bonus.
The hardened version loads it from an environment variable and
enforces a minimum entropy check at startup.
"""

import os
from flask import Flask, render_template

from auth      import auth_bp
from accounts  import accounts_bp
from transfers import transfers_bp
from admin     import admin_bp


def create_app() -> Flask:
    app = Flask(__name__, template_folder="templates")

    # ── Session / Security config ──────────────────────────────────────────
    # BONUS VULNERABILITY: Hardcoded, weak secret key in source code.
    # Any developer with repo access can forge session cookies.
    # Fix: load from env → SECRET_KEY = os.environ["SECRET_KEY"]
    app.config["SECRET_KEY"] = "supersecret123"       # ← insecure, intentional
    app.config["SESSION_COOKIE_HTTPONLY"] = True
    app.config["SESSION_COOKIE_SAMESITE"] = "Lax"
    # Note: SECURE flag is off because this runs on HTTP in the lab.
    # Hardened version enables it when behind HTTPS.
    app.config["SESSION_COOKIE_SECURE"] = False

    # ── Register blueprints ────────────────────────────────────────────────
    app.register_blueprint(auth_bp)
    app.register_blueprint(accounts_bp)
    app.register_blueprint(transfers_bp)
    app.register_blueprint(admin_bp)

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
        return render_template("error.html", code=500,
                               message="Internal server error."), 500

    return app


if __name__ == "__main__":
    app = create_app()
    app.run(
        host="0.0.0.0",
        port=int(os.environ.get("PORT", 5000)),
        debug=True    # debug=True is intentional for demo — shows stack traces
    )
