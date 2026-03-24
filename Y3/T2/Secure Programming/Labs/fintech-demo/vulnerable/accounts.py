"""
accounts.py - Account and dashboard routes for the VULNERABLE demo app.

══════════════════════════════════════════════════════════
VULNERABILITY 2: Insecure Direct Object Reference (IDOR)  (CWE-639)
══════════════════════════════════════════════════════════

HOW IT WORKS:
  The /account/<account_id> route fetches ANY account by ID
  without checking whether the currently logged-in user OWNS that account.
  An attacker who knows (or guesses) another account's ID can view it.

DEMO EXPLOIT:
  1. Alice logs in → her account is ACC001
  2. Alice visits /account/ACC004 in the browser
  3. She sees the admin's account with $50,000 balance
  4. She can enumerate ACC001, ACC002 ... ACC006 freely

WHY IT'S DANGEROUS:
  • Exposes other users' balances and transaction history
  • Account IDs are short sequential strings → trivially guessable
  • Chained with VULN 3: Alice now knows the admin account ID to steal from

SECURE FIX (see hardened/accounts.py):
  After fetching the account, verify account.user_id == session["user_id"].
  Return 403 if ownership check fails.

SECURE CODING PRINCIPLE:
  "Always perform authorization checks at the resource level,
   not just at the route level."
  (OWASP Top 10 A01:2021 – Broken Access Control)
"""

from flask import Blueprint, render_template, session, redirect, url_for, abort
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
    # Only fetch accounts owned by this user — correct for the dashboard
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
    !! VULNERABILITY 2: IDOR — No ownership check !!

    We fetch the account by primary key only.
    We never verify: does session["user_id"] == account["user_id"]?

    Any authenticated user can visit /account/ACC004 and see admin's data.
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

    # MISSING: ownership check should go here
    # SECURE CODE WOULD BE:
    # if account["user_id"] != session["user_id"] and session["role"] != "admin":
    #     abort(403)

    transactions = db.query_all("""
        SELECT * FROM transactions
        WHERE from_account=? OR to_account=?
        ORDER BY created_at DESC
    """, (account_id, account_id))

    # Log the view — logger flags it as suspicious if viewer != owner
    log.account_viewed(
        viewer=session["username"],
        account_id=account_id,
        owner=account["owner"],
        ip="127.0.0.1"
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
