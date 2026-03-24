"""
logger.py - Structured JSON logging for Wazuh ingestion.
HARDENED VERSION — functionally identical to vulnerable logger,
but app_version reflects the hardened build.

See vulnerable/logger.py for full documentation of the log schema.
"""

import json
import logging
import os
import sys
from datetime import datetime, timezone

LOG_PATH    = os.environ.get("LOG_PATH", "/app/logs/app.log")
APP_VERSION = "2.0.0-hardened"


class WazuhJSONFormatter(logging.Formatter):
    def format(self, record: logging.LogRecord) -> str:
        payload = {
            "timestamp":   datetime.now(timezone.utc).isoformat(),
            "level":       record.levelname,
            "app":         "fintechdemo",
            "app_version": APP_VERSION,
            "event_type":  getattr(record, "event_type", "general"),
            "user":        getattr(record, "user",       "anonymous"),
            "ip":          getattr(record, "ip",         "unknown"),
            "message":     record.getMessage(),
            "detail":      getattr(record, "detail",     {}),
        }
        return json.dumps(payload)


def _build_logger() -> logging.Logger:
    logger = logging.getLogger("fintechdemo")
    logger.setLevel(logging.DEBUG)
    if logger.handlers:
        return logger
    formatter = WazuhJSONFormatter()
    sh = logging.StreamHandler(sys.stdout)
    sh.setFormatter(formatter)
    logger.addHandler(sh)
    log_dir = os.path.dirname(LOG_PATH)
    if log_dir and os.path.isdir(log_dir):
        fh = logging.FileHandler(LOG_PATH)
        fh.setFormatter(formatter)
        logger.addHandler(fh)
    return logger


_logger = _build_logger()


def _log(level, event_type, message, user="anonymous", ip="unknown", detail=None):
    extra = {"event_type": event_type, "user": user, "ip": ip, "detail": detail or {}}
    getattr(_logger, level)(message, extra=extra)


def login_success(username, ip):
    _log("info", "auth.login_success", f"Successful login for '{username}'",
         user=username, ip=ip)


def login_failure(username, ip, reason="bad_credentials"):
    _log("warning", "auth.login_failure",
         f"Failed login attempt for '{username}' - {reason}",
         user=username, ip=ip, detail={"reason": reason})


def logout(username, ip):
    _log("info", "auth.logout", f"User '{username}' logged out",
         user=username, ip=ip)


def account_viewed(viewer, account_id, owner, ip):
    suspicious = viewer != owner
    level = "warning" if suspicious else "info"
    _log(level, "account.viewed",
         f"Account {account_id} viewed by '{viewer}' (owner: '{owner}')",
         user=viewer, ip=ip,
         detail={"account_id": account_id, "owner": owner,
                 "suspicious": suspicious})


def transfer_initiated(initiator, from_account, to_account, amount, ip,
                       authorized=True):
    event = "transfer.completed" if authorized else "transfer.unauthorized"
    level = "info" if authorized else "critical"
    _log(level, event,
         f"Transfer ${amount:.2f} from {from_account} to {to_account} "
         f"by '{initiator}' (authorized={authorized})",
         user=initiator, ip=ip,
         detail={"from_account": from_account, "to_account": to_account,
                 "amount": amount, "authorized": authorized})


def admin_action(admin, action, target, ip):
    _log("info", "admin.action",
         f"Admin '{admin}' performed '{action}' on {target}",
         user=admin, ip=ip, detail={"action": action, "target": target})


def security_event(event_type, message, user="anonymous", ip="unknown",
                   detail=None):
    _log("warning", f"security.{event_type}", message, user=user, ip=ip,
         detail=detail or {})
