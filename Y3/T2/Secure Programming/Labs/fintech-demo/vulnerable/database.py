"""
database.py - SQLite connection and query helpers.

FOR EDUCATIONAL USE ONLY.
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
    """Execute a query and return a single row."""
    with get_db() as conn:
        cur = conn.execute(sql, params)
        return cur.fetchone()


def query_all(sql: str, params: tuple = ()) -> list:
    """Execute a query and return all rows."""
    with get_db() as conn:
        cur = conn.execute(sql, params)
        return cur.fetchall()


def execute(sql: str, params: tuple = ()) -> int:
    """Execute a write query and return lastrowid."""
    with get_db() as conn:
        cur = conn.execute(sql, params)
        conn.commit()
        return cur.lastrowid


def execute_unsafe(sql: str) -> list:
    """
    !! VULNERABILITY: Executes raw SQL string with no parameterization !!
    This function exists ONLY to demonstrate SQL injection.
    It directly embeds user input into the query string.

    DO NOT USE THIS PATTERN IN PRODUCTION.
    The hardened version removes this function entirely and
    uses parameterized queries everywhere.
    """
    with get_db() as conn:
        cur = conn.execute(sql)
        return cur.fetchall()
