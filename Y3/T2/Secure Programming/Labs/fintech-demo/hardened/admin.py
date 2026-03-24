"""
admin.py - Admin panel routes for the HARDENED demo app.
Functionally identical to the vulnerable version's admin panel,
but it now benefits from the fixed authentication (VULN 1 fixed),
so SQLi cannot grant admin access.
"""

from flask import Blueprint, render_template, session, redirect, url_for, abort, request
from functools import wraps
import database as db
import logger as log

admin_bp = Blueprint("admin", __name__)


def admin_required(f):
    @wraps(f)
    def decorated(*args, **kwargs):
        if "user_id" not in session:
            return redirect(url_for("auth.login"))
        if session.get("role") != "admin":
            log.security_event(
                "unauthorized_admin_access",
                f"User '{session.get('username')}' attempted admin access",
                user=session.get("username"),
                ip=request.remote_addr
            )
            abort(403)
        return f(*args, **kwargs)
    return decorated


@admin_bp.route("/admin")
@admin_required
def admin_panel():
    users    = db.query_all("SELECT * FROM users ORDER BY id")
    accounts = db.query_all(
        "SELECT a.*, u.username FROM accounts a JOIN users u ON a.user_id=u.id"
    )
    all_tx   = db.query_all(
        "SELECT * FROM transactions ORDER BY created_at DESC LIMIT 50"
    )

    log.admin_action(
        admin=session["username"],
        action="view_admin_panel",
        target="all_users_accounts",
        ip=request.remote_addr
    )

    return render_template("admin.html",
                           users=users,
                           accounts=accounts,
                           transactions=all_tx)
