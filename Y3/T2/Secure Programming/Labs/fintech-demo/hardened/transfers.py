"""
transfers.py - Money transfer routes for the HARDENED demo app.

══════════════════════════════════════════════════════════
FIX 3: Missing Authorization → Server-Side Ownership Check  (CWE-862)
══════════════════════════════════════════════════════════

Changes from vulnerable version:
  After fetching the source account, the server immediately verifies:

      if from_account["user_id"] != session["user_id"]:
          log unauthorized attempt
          abort(403)

  Additionally:
  - The "from_account" dropdown in the UI only shows the user's own accounts,
    but the server NEVER trusts the UI. The check is always performed regardless
    of what the form sends.
  - A CSRF token would be added in a full production implementation
    (Flask-WTF / flask-seasurf). Omitted here to keep the demo stack minimal,
    but noted in the DevSecOps checklist.
  - Amount is validated as a positive Decimal (not float) to avoid
    floating-point rounding issues that could be exploited in race conditions.
"""

from decimal import Decimal, InvalidOperation
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

    my_accounts  = db.query_all(
        "SELECT * FROM accounts WHERE user_id=? AND is_frozen=0", (user_id,)
    )
    all_accounts = db.query_all(
        "SELECT id, user_id FROM accounts WHERE is_frozen=0"
    )

    if request.method == "GET":
        return render_template("transfer.html",
                               my_accounts=my_accounts,
                               all_accounts=all_accounts)

    # ── Parse and validate input ───────────────────────────────────────────
    from_account_id = request.form.get("from_account", "").strip()
    to_account_id   = request.form.get("to_account",   "").strip()
    description     = request.form.get("description",  "").strip()[:120]

    try:
        amount = Decimal(request.form.get("amount", "0"))
    except InvalidOperation:
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
    # FIX 3: SERVER-SIDE OWNERSHIP CHECK
    #
    # Regardless of what the client sent in the form body, we verify that
    # the from_account is owned by the currently authenticated user.
    # This check happens server-side in Python, against the authoritative
    # database record — it cannot be bypassed by:
    #   - Editing the HTML form
    #   - Crafting a raw curl/HTTP request
    #   - Replaying a captured request with modified fields
    # ══════════════════════════════════════════════════════════════════════
    if from_account["user_id"] != user_id:
        log.transfer_initiated(
            username, from_account_id, to_account_id,
            float(amount), ip, authorized=False
        )
        log.security_event(
            "unauthorized_transfer_attempt",
            f"'{username}' attempted to transfer from account "
            f"{from_account_id} (owned by user #{from_account['user_id']})",
            user=username, ip=ip,
            detail={"from_account": from_account_id,
                    "to_account": to_account_id,
                    "amount": str(amount)}
        )
        abort(403)
    # ══════════════════════════════════════════════════════════════════════

    to_account = db.query_one(
        "SELECT * FROM accounts WHERE id=? AND is_frozen=0", (to_account_id,)
    )
    if not to_account:
        flash("Destination account not found or is frozen.", "danger")
        return render_template("transfer.html",
                               my_accounts=my_accounts,
                               all_accounts=all_accounts)

    if Decimal(str(from_account["balance"])) < amount:
        flash("Insufficient funds.", "warning")
        return render_template("transfer.html",
                               my_accounts=my_accounts,
                               all_accounts=all_accounts)

    # ── Execute transfer (both updates in one connection for atomicity) ────
    import database as _db_raw
    conn = _db_raw.get_db()
    try:
        conn.execute(
            "UPDATE accounts SET balance = balance - ? WHERE id=?",
            (str(amount), from_account_id)
        )
        conn.execute(
            "UPDATE accounts SET balance = balance + ? WHERE id=?",
            (str(amount), to_account_id)
        )
        conn.execute("""
            INSERT INTO transactions
                (from_account, to_account, amount, description, tx_type, status, initiated_by)
            VALUES (?, ?, ?, ?, 'transfer', 'completed', ?)
        """, (from_account_id, to_account_id, str(amount),
              description or "Transfer", user_id))
        conn.commit()
    except Exception as e:
        conn.rollback()
        log.security_event("transfer_db_error", str(e), user=username, ip=ip)
        flash("Transfer failed due to a system error. Please try again.", "danger")
        return render_template("transfer.html",
                               my_accounts=my_accounts,
                               all_accounts=all_accounts)
    finally:
        conn.close()

    log.transfer_initiated(username, from_account_id, to_account_id,
                           float(amount), ip, authorized=True)

    flash(f"Transfer of ${float(amount):,.2f} from {from_account_id} to "
          f"{to_account_id} completed.", "success")
    return redirect(url_for("accounts.dashboard"))
