"""
accounts.py - Account and dashboard routes for the HARDENED demo app.

══════════════════════════════════════════════════════════
FIX 2: IDOR → Server-Side Ownership Enforcement  (CWE-639)
══════════════════════════════════════════════════════════

Changes from vulnerable version:
  After fetching any account by ID, the server immediately checks:

      if account["user_id"] != session["user_id"]
          AND session["role"] != "admin":
          abort(403)

  This check runs in Python, server-side, before any data is rendered.
  There is no way for a client to bypass it by manipulating URLs or
  HTTP headers — the authoritative ownership record is in the database,
  compared against a server-managed session value.

  Admins retain access to all accounts for support purposes, and every
  cross-user view by an admin is logged (see logger.account_viewed).
"""

from flask import (Blueprint, render_template, session, redirect,
                   url_for, abort, request)
from functools import wraps
import database as db
import logger as log

accounts_bp = Blueprint("accounts", __name__)


def login_required(f):
    @wraps(f)
    def decorated(*args, **kwargs):
        if "user_id" not in session:
            return redirect(url_for("auth.login"))
        return f(*args, **kwargs)
    return decorated


@accounts_bp.route("/dashboard")
@login_required
def dashboard():
    user_id = session["user_id"]
    user_accounts = db.query_all(
        "SELECT * FROM accounts WHERE user_id=? ORDER BY id", (user_id,)
    )
    recent_tx = db.query_all("""
        SELECT t.*, a1.id AS fa_id, a2.id AS ta_id
        FROM transactions t
        LEFT JOIN accounts a1 ON t.from_account = a1.id
        LEFT JOIN accounts a2 ON t.to_account   = a2.id
        WHERE a1.user_id=? OR a2.user_id=?
        ORDER BY t.created_at DESC
        LIMIT 5
    """, (user_id, user_id))

    return render_template("dashboard.html",
                           accounts=user_accounts,
                           recent_tx=recent_tx)


@accounts_bp.route("/account/<account_id>")
@login_required
def account_detail(account_id):
    """
    ══════════════════════════════════════════════════════════════════
    FIX 2: Ownership check added immediately after DB fetch.

    Flow:
      1. Fetch account (if 404 → 404 response)
      2. Compare account.user_id with session.user_id
      3. If mismatch AND not admin → 403 Forbidden
      4. Log the view (logger flags cross-user views as suspicious)
      5. Only then render the template
    ══════════════════════════════════════════════════════════════════
    """
    account = db.query_one(
        "SELECT a.*, u.username AS owner FROM accounts a "
        "JOIN users u ON a.user_id = u.id "
        "WHERE a.id=?",
        (account_id,)
    )

    if not account:
        abort(404)

    # ── OWNERSHIP ENFORCEMENT ──────────────────────────────────────────────
    is_owner = (account["user_id"] == session["user_id"])
    is_admin  = (session.get("role") == "admin")

    if not is_owner and not is_admin:
        log.security_event(
            "idor_attempt",
            f"User '{session['username']}' tried to access account "
            f"{account_id} owned by '{account['owner']}'",
            user=session["username"],
            ip=request.remote_addr,
            detail={"target_account": account_id,
                    "owner": account["owner"]}
        )
        abort(403)
    # ── END OWNERSHIP ENFORCEMENT ──────────────────────────────────────────

    transactions = db.query_all("""
        SELECT * FROM transactions
        WHERE from_account=? OR to_account=?
        ORDER BY created_at DESC
    """, (account_id, account_id))

    log.account_viewed(
        viewer=session["username"],
        account_id=account_id,
        owner=account["owner"],
        ip=request.remote_addr
    )

    return render_template("account_detail.html",
                           account=account,
                           transactions=transactions)


@accounts_bp.route("/history")
@login_required
def history():
    user_id = session["user_id"]
    all_tx = db.query_all("""
        SELECT t.*, a1.id AS from_id, a2.id AS to_id
        FROM transactions t
        LEFT JOIN accounts a1 ON t.from_account = a1.id
        LEFT JOIN accounts a2 ON t.to_account   = a2.id
        WHERE a1.user_id=? OR a2.user_id=?
        ORDER BY t.created_at DESC
    """, (user_id, user_id))

    return render_template("history.html", transactions=all_tx)
