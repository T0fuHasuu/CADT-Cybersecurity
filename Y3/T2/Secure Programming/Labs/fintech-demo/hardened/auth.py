"""
auth.py - Authentication routes for the HARDENED demo app.

══════════════════════════════════════════════════════════
FIX 1: SQL Injection → Parameterized Queries  (CWE-89)
FIX 2: Insecure Password Storage → bcrypt     (CWE-916)
══════════════════════════════════════════════════════════

Changes from vulnerable version:
  1. execute_unsafe() replaced with query_one() using (?, ?) placeholders.
     User input is passed as a separate tuple — the SQLite driver escapes
     it before substitution. Injecting admin'-- into username is harmless:
     the entire string (including quote and dash) becomes the literal search value.

  2. MD5 replaced with bcrypt.checkpw(). We fetch the user record by username
     first (safe), then verify the password hash with bcrypt's constant-time
     compare. The timing is identical for valid and invalid usernames (we use
     a dummy hash check to prevent username enumeration via timing).

  3. Login attempts are rate-limited in memory (5 failures / 5 min per IP).
     A real deployment would use Redis for distributed rate limiting.

  4. Hardcoded SECRET_KEY replaced with os.environ["SECRET_KEY"] (see app.py).
"""

import os
import time
from collections import defaultdict
from flask import (Blueprint, render_template, request, session,
                   redirect, url_for, flash)

try:
    import bcrypt
except ImportError:
    raise RuntimeError("bcrypt not installed. Run: pip install bcrypt")

import database as db
import logger as log

auth_bp = Blueprint("auth", __name__)

# ── In-memory rate limiter: {ip: [(timestamp, …), …]} ──────────────────────
_FAILED: dict[str, list[float]] = defaultdict(list)
MAX_FAILURES  = 5
WINDOW_SECS   = 300  # 5 minutes

# Dummy bcrypt hash for constant-time password check on unknown usernames
# (prevents username enumeration via response timing)
_DUMMY_HASH = bcrypt.hashpw(b"dummy", bcrypt.gensalt(rounds=12)).decode()


def _is_rate_limited(ip: str) -> bool:
    cutoff = time.time() - WINDOW_SECS
    _FAILED[ip] = [t for t in _FAILED[ip] if t > cutoff]
    return len(_FAILED[ip]) >= MAX_FAILURES


def _record_failure(ip: str):
    _FAILED[ip].append(time.time())


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
    ip       = request.remote_addr

    # ── Rate limit check ───────────────────────────────────────────────────
    if _is_rate_limited(ip):
        log.login_failure(username, ip, reason="rate_limited")
        flash("Too many failed attempts. Please wait 5 minutes.", "danger")
        return render_template("login.html"), 429

    # ── FIX 1: Parameterized query — injection-proof ───────────────────────
    #
    # The (?, ) placeholder causes the SQLite driver to treat username as
    # a data value, not SQL syntax. Even "admin'--" is passed as a literal
    # string to the WHERE clause — the comment never executes.
    #
    user = db.query_one(
        "SELECT * FROM users WHERE username=? AND is_active=1",
        (username,)
    )

    # ── FIX 2: bcrypt constant-time verification ───────────────────────────
    #
    # We always call bcrypt.checkpw(), even for unknown usernames (using the
    # dummy hash). This prevents an attacker from detecting valid usernames
    # by measuring whether the response comes back faster (no hash check)
    # or slower (hash check).
    #
    stored_hash = user["password"].encode() if user else _DUMMY_HASH.encode()
    password_ok = bcrypt.checkpw(password.encode(), stored_hash)

    if not user or not password_ok:
        _record_failure(ip)
        log.login_failure(username, ip)
        # Generic message — don't reveal whether username or password was wrong
        flash("Invalid username or password.", "danger")
        return render_template("login.html")

    # Success — regenerate session to prevent session fixation
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
