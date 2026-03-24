"""
transfers.py - Money transfer routes for the VULNERABLE demo app.

══════════════════════════════════════════════════════════
VULNERABILITY 3: Missing Server-Side Authorization on Transfer  (CWE-862)
══════════════════════════════════════════════════════════

HOW IT WORKS:
  The transfer endpoint accepts `from_account` from the POST body.
  It never checks whether the logged-in user actually OWNS that account.
  An attacker can change the hidden form field (or craft a raw POST)
  to initiate a transfer FROM any account — including accounts they don't own.

DEMO EXPLOIT (the climax of the attack chain):
  1. Alice is logged in (owns ACC001)
  2. VULN 2 (IDOR) revealed: admin owns ACC004 with $50,000
  3. Alice opens the transfer form (from_account=ACC001)
  4. Alice changes the hidden field to from_account=ACC004
  5. She transfers $10,000 from admin's account to ACC001
  6. Server accepts it — no ownership check performed

DEMO CURL COMMAND (bypass the form entirely):
  curl -X POST http://localhost:5000/transfer \
    -b "session=<alice_session_cookie>" \
    -d "from_account=ACC004&to_account=ACC001&amount=10000&description=test"

WHY IT'S DANGEROUS:
  • Direct financial loss
  • Breaks non-repudiation — alice's session, but admin's money
  • Chains perfectly with IDOR to discover victim account IDs

SECURE FIX (see hardened/transfers.py):
  After parsing from_account, fetch the account from DB and verify
  account.user_id == session["user_id"]. Return 403 if not.

SECURE CODING PRINCIPLE:
  "Never trust client-supplied identifiers for authorization decisions.
   Always verify ownership server-side."
  (OWASP Top 10 A01:2021 – Broken Access Control)
"""

from flask import (Blueprint, render_template, request, session,
                   redirect, url_for, flash, abort)
from functools import wraps
import database as db
import logger as log

transfers_bp = Blueprint("transfers", __name__)


def login_required(f):
    @wraps(f)
    def decorated(*args, **kwargs):
        if "user_id" not in session:
            return redirect(url_for("auth.login"))
        return f(*args, **kwargs)
    return decorated


@transfers_bp.route("/transfer", methods=["GET", "POST"])
@login_required
def transfer():
    user_id  = session["user_id"]
    username = session["username"]
    ip       = request.remote_addr

    # Fetch accounts the user legitimately owns (for the dropdown)
    my_accounts = db.query_all(
        "SELECT * FROM accounts WHERE user_id=? AND is_frozen=0",
        (user_id,)
    )
    all_accounts = db.query_all(
        "SELECT id, user_id FROM accounts WHERE is_frozen=0"
    )

    if request.method == "GET":
        return render_template("transfer.html",
                               my_accounts=my_accounts,
                               all_accounts=all_accounts)

    # ── Parse form input ───────────────────────────────────────────────────
    from_account_id = request.form.get("from_account", "").strip()
    to_account_id   = request.form.get("to_account",   "").strip()
    description     = request.form.get("description",  "").strip()

    try:
        amount = float(request.form.get("amount", 0))
    except ValueError:
        flash("Invalid amount.", "danger")
        return render_template("transfer.html",
                               my_accounts=my_accounts,
                               all_accounts=all_accounts)

    if amount <= 0:
        flash("Amount must be positive.", "danger")
        return render_template("transfer.html",
                               my_accounts=my_accounts,
                               all_accounts=all_accounts)

    if from_account_id == to_account_id:
        flash("Cannot transfer to the same account.", "danger")
        return render_template("transfer.html",
                               my_accounts=my_accounts,
                               all_accounts=all_accounts)

    # ── Fetch source account ───────────────────────────────────────────────
    from_account = db.query_one(
        "SELECT * FROM accounts WHERE id=?", (from_account_id,)
    )
    if not from_account:
        flash("Source account not found.", "danger")
        return render_template("transfer.html",
                               my_accounts=my_accounts,
                               all_accounts=all_accounts)

    # ══════════════════════════════════════════════════════════════════════
    # !! VULNERABILITY 3: MISSING OWNERSHIP CHECK !!
    #
    # We have from_account.user_id available, but we NEVER compare it
    # against session["user_id"]. Any logged-in user can submit any
    # from_account value and the server will process it.
    #
    # SECURE CODE WOULD BE:
    # if from_account["user_id"] != user_id:
    #     log.transfer_initiated(username, from_account_id, to_account_id,
    #                            amount, ip, authorized=False)
    #     abort(403)
    # ══════════════════════════════════════════════════════════════════════

    # Fetch destination account
    to_account = db.query_one(
        "SELECT * FROM accounts WHERE id=? AND is_frozen=0",
        (to_account_id,)
    )
    if not to_account:
        flash("Destination account not found or is frozen.", "danger")
        return render_template("transfer.html",
                               my_accounts=my_accounts,
                               all_accounts=all_accounts)

    # Balance check (only checks balance, not ownership)
    if from_account["balance"] < amount:
        flash("Insufficient funds.", "warning")
        return render_template("transfer.html",
                               my_accounts=my_accounts,
                               all_accounts=all_accounts)

    # ── Execute transfer ───────────────────────────────────────────────────
    db.execute(
        "UPDATE accounts SET balance = balance - ? WHERE id=?",
        (amount, from_account_id)
    )
    db.execute(
        "UPDATE accounts SET balance = balance + ? WHERE id=?",
        (amount, to_account_id)
    )
    db.execute("""
        INSERT INTO transactions
            (from_account, to_account, amount, description, tx_type, status, initiated_by)
        VALUES (?, ?, ?, ?, 'transfer', 'completed', ?)
    """, (from_account_id, to_account_id, amount,
          description or "Transfer", user_id))

    # Log with authorized=True because we never actually checked ownership
    # (In the hardened version this would log authorized=False and abort)
    log.transfer_initiated(username, from_account_id, to_account_id,
                           amount, ip, authorized=True)

    flash(f"Transfer of ${amount:,.2f} from {from_account_id} to "
          f"{to_account_id} completed.", "success")
    return redirect(url_for("accounts.dashboard"))
