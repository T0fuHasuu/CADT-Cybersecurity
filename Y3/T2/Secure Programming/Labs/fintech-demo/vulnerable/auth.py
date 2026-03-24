"""
auth.py - Authentication routes for the VULNERABLE demo app.

══════════════════════════════════════════════════════════
VULNERABILITY 1: SQL Injection in Login Form  (CWE-89)
══════════════════════════════════════════════════════════

HOW IT WORKS:
  The login query is built by direct string formatting of user input.
  An attacker can inject SQL meta-characters to alter the query logic.

DEMO PAYLOAD:
  Username: admin'--
  Password: (anything)

  Resulting query:
    SELECT * FROM users WHERE username='admin'--' AND password='...'
  The '--' comments out the password check entirely → login as admin.

WHY IT'S DANGEROUS:
  • Authentication bypass without knowing any password
  • Can be extended to data exfiltration (UNION-based injection)
  • Leads directly to VULN 2 (IDOR) once logged in as admin

SECURE FIX (see hardened/auth.py):
  Use parameterized queries: db.query_one(sql, (username, password_hash))
  The database driver escapes all user input before interpolation.

SECURE CODING PRINCIPLE:
  "Never concatenate user-controlled data into query strings."
  (OWASP Top 10 A03:2021 – Injection)
"""

import hashlib
from flask import Blueprint, render_template, request, session, redirect, url_for, flash
import database as db
import logger as log

auth_bp = Blueprint("auth", __name__)


def md5(s: str) -> str:
    return hashlib.md5(s.encode()).hexdigest()


# ── Simple SQLi heuristic (for logging only — does NOT block the attack) ──────
SQLI_CHARS = ["'", '"', "--", ";", "/*", "*/", "xp_", "UNION", "OR 1=1"]


def looks_like_sqli(value: str) -> bool:
    upper = value.upper()
    return any(c.upper() in upper for c in SQLI_CHARS)


# ── Routes ────────────────────────────────────────────────────────────────────

@auth_bp.route("/", methods=["GET"])
def index():
    if "user_id" in session:
        return redirect(url_for("accounts.dashboard"))
    return redirect(url_for("auth.login"))


@auth_bp.route("/login", methods=["GET", "POST"])
def login():
    if "user_id" in session:
        return redirect(url_for("accounts.dashboard"))

    if request.method == "GET":
        return render_template("login.html")

    username = request.form.get("username", "").strip()
    password = request.form.get("password", "")
    ip = request.remote_addr

    # Heuristic log (does NOT prevent the injection — intentional for demo)
    if looks_like_sqli(username):
        log.sql_injection_attempt(username, ip)

    # ══════════════════════════════════════════════════════════════════
    # !! VULNERABILITY 1: SQL INJECTION !!
    #
    # User input is embedded directly into the SQL string via f-string.
    # No parameterization. No escaping.
    #
    # Payload: username = admin'--
    # Built query:
    #   SELECT * FROM users
    #   WHERE username='admin'--' AND password='...'
    #
    # The '--' turns the rest into a comment. Password check is skipped.
    # ══════════════════════════════════════════════════════════════════
    password_hash = md5(password)
    unsafe_sql = (
        f"SELECT * FROM users "
        f"WHERE username='{username}' "
        f"AND password='{password_hash}' "
        f"AND is_active=1"
    )

    try:
        rows = db.execute_unsafe(unsafe_sql)
    except Exception as e:
        log.login_failure(username, ip, reason=f"db_error: {e}")
        flash("Database error. Please try again.", "danger")
        return render_template("login.html")

    if not rows:
        log.login_failure(username, ip)
        flash("Invalid username or password.", "danger")
        return render_template("login.html")

    user = rows[0]

    # Populate session
    session.clear()
    session["user_id"]   = user["id"]
    session["username"]  = user["username"]
    session["full_name"] = user["full_name"]
    session["role"]      = user["role"]

    log.login_success(user["username"], ip)
    return redirect(url_for("accounts.dashboard"))


@auth_bp.route("/logout")
def logout():
    username = session.get("username", "anonymous")
    log.logout(username, request.remote_addr)
    session.clear()
    flash("You have been logged out.", "info")
    return redirect(url_for("auth.login"))
