"""
logger.py - Structured JSON logging for Wazuh ingestion.

Log format is JSON-per-line (NDJSON), compatible with Wazuh file monitoring
via /var/ossec/etc/ossec.conf localfile → format: json.

Each event includes:
  timestamp, level, event_type, user, ip, detail, app_version
"""

import json
import logging
import os
import sys
from datetime import datetime, timezone

LOG_PATH = os.environ.get("LOG_PATH", "/app/logs/app.log")
APP_VERSION = "1.0.0-vulnerable"


class WazuhJSONFormatter(logging.Formatter):
    """Formats log records as Wazuh-ingestable JSON lines."""

    def format(self, record: logging.LogRecord) -> str:
        payload = {
            "timestamp":  datetime.now(timezone.utc).isoformat(),
            "level":      record.levelname,
            "app":        "fintechdemo",
            "app_version": APP_VERSION,
            "event_type": getattr(record, "event_type", "general"),
            "user":       getattr(record, "user", "anonymous"),
            "ip":         getattr(record, "ip", "unknown"),
            "message":    record.getMessage(),
            "detail":     getattr(record, "detail", {}),
        }
        return json.dumps(payload)


def _build_logger() -> logging.Logger:
    logger = logging.getLogger("fintechdemo")
    logger.setLevel(logging.DEBUG)

    if logger.handlers:
        return logger

    formatter = WazuhJSONFormatter()

    # Always log to stdout (Docker-friendly)
    sh = logging.StreamHandler(sys.stdout)
    sh.setFormatter(formatter)
    logger.addHandler(sh)

    # Log to file if directory exists
    log_dir = os.path.dirname(LOG_PATH)
    if log_dir and os.path.isdir(log_dir):
        fh = logging.FileHandler(LOG_PATH)
        fh.setFormatter(formatter)
        logger.addHandler(fh)

    return logger


_logger = _build_logger()


def _log(level: str, event_type: str, message: str, user: str = "anonymous",
         ip: str = "unknown", detail: dict = None):
    extra = {"event_type": event_type, "user": user, "ip": ip, "detail": detail or {}}
    getattr(_logger, level)(message, extra=extra)


# ── Public helpers ────────────────────────────────────────────────────────────

def login_success(username: str, ip: str):
    _log("info", "auth.login_success", f"Successful login for '{username}'",
         user=username, ip=ip)


def login_failure(username: str, ip: str, reason: str = "bad_credentials"):
    _log("warning", "auth.login_failure",
         f"Failed login attempt for '{username}' - {reason}",
         user=username, ip=ip, detail={"reason": reason})


def logout(username: str, ip: str):
    _log("info", "auth.logout", f"User '{username}' logged out",
         user=username, ip=ip)


def account_viewed(viewer: str, account_id: str, owner: str, ip: str):
    """
    SECURITY RELEVANCE: This log captures IDOR exploits.
    If viewer != owner, this is suspicious and Wazuh can alert on it.
    """
    suspicious = viewer != owner
    level = "warning" if suspicious else "info"
    _log(level, "account.viewed",
         f"Account {account_id} viewed by '{viewer}' (owner: '{owner}')",
         user=viewer, ip=ip,
         detail={"account_id": account_id, "owner": owner,
                 "suspicious": suspicious})


def transfer_initiated(initiator: str, from_account: str, to_account: str,
                       amount: float, ip: str, authorized: bool = True):
    """
    SECURITY RELEVANCE: authorized=False flags an unauthorized transfer attempt.
    Wazuh rule can alert on event_type=transfer.unauthorized.
    """
    event = "transfer.completed" if authorized else "transfer.unauthorized"
    level = "info" if authorized else "critical"
    _log(level, event,
         f"Transfer ${amount:.2f} from {from_account} to {to_account} "
         f"by '{initiator}' (authorized={authorized})",
         user=initiator, ip=ip,
         detail={"from_account": from_account, "to_account": to_account,
                 "amount": amount, "authorized": authorized})


def admin_action(admin: str, action: str, target: str, ip: str):
    _log("info", "admin.action", f"Admin '{admin}' performed '{action}' on {target}",
         user=admin, ip=ip, detail={"action": action, "target": target})


def sql_injection_attempt(username_input: str, ip: str):
    """Logged when heuristic detects SQLi chars in login input."""
    _log("critical", "security.sqli_attempt",
         "Possible SQL injection in login form",
         ip=ip, detail={"raw_input": username_input[:120]})


def security_event(event_type: str, message: str, user: str = "anonymous",
                   ip: str = "unknown", detail: dict = None):
    _log("warning", f"security.{event_type}", message, user=user, ip=ip,
         detail=detail or {})
