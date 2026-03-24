"""
admin.py - Admin panel routes for the VULNERABLE demo app.

Note: The admin panel itself is access-controlled (role check),
but it relies on the session role which was set at login.
After a SQLi bypass, an attacker who "logs in" as admin gets full
admin privileges because the role is read from the compromised user record.
"""

from flask import Blueprint, render_template, session, redirect, url_for, abort
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
                ip="127.0.0.1"
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
        ip="127.0.0.1"
    )

    return render_template("admin.html",
                           users=users,
                           accounts=accounts,
                           transactions=all_tx)
