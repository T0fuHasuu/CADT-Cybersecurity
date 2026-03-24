"""
database.py - SQLite connection and query helpers.
HARDENED VERSION: execute_unsafe() has been removed entirely.

All queries use parameterized placeholders (?), so the SQLite driver
handles escaping. User-supplied strings can never alter query structure.
"""

import sqlite3
import os
from typing import Optional

DB_PATH = os.environ.get("DB_PATH", "bank.db")


def get_db() -> sqlite3.Connection:
    conn = sqlite3.connect(DB_PATH)
    conn.row_factory = sqlite3.Row
    conn.execute("PRAGMA journal_mode=WAL")
    conn.execute("PRAGMA foreign_keys=ON")
    return conn


def query_one(sql: str, params: tuple = ()) -> Optional[sqlite3.Row]:
    with get_db() as conn:
        cur = conn.execute(sql, params)
        return cur.fetchone()


def query_all(sql: str, params: tuple = ()) -> list:
    with get_db() as conn:
        cur = conn.execute(sql, params)
        return cur.fetchall()


def execute(sql: str, params: tuple = ()) -> int:
    with get_db() as conn:
        cur = conn.execute(sql, params)
        conn.commit()
        return cur.lastrowid

# NOTE: execute_unsafe() intentionally absent.
# The vulnerable version's execute_unsafe() allowed raw string injection.
# With only parameterized functions available, developers cannot accidentally
# use an unsafe path even if they tried.
