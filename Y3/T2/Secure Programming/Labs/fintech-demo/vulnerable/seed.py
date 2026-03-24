#!/usr/bin/env python3
"""
seed.py - Populate the database with fake demo data.
VULNERABLE VERSION: passwords stored as MD5 (insecure).

FOR EDUCATIONAL USE ONLY - all users, balances, and transactions are fictional.
"""

import sqlite3
import hashlib
import os
import sys

DB_PATH = os.environ.get("DB_PATH", "bank.db")
SCHEMA_PATH = os.path.join(os.path.dirname(__file__), "schema.sql")


def md5(password: str) -> str:
    """
    VULNERABILITY NOTE (Insecure Password Storage):
    MD5 is a fast hashing algorithm with no salt, making it trivially
    crackable via rainbow tables or brute force. This is intentionally
    insecure for demonstration. The hardened version uses bcrypt with
    per-user salts and a work factor of 12.
    """
    return hashlib.md5(password.encode()).hexdigest()


def seed():
    conn = sqlite3.connect(DB_PATH)
    conn.row_factory = sqlite3.Row
    cur = conn.cursor()

    # Apply schema
    with open(SCHEMA_PATH, "r") as f:
        conn.executescript(f.read())

    # ------------------------------------------------------------------
    # FAKE USERS (role, username, plain_password → stored as MD5)
    # ------------------------------------------------------------------
    users = [
        (1, "alice",  md5("password123"), "Alice Chen",    "alice@fintechdemo.local",  "user",  1),
        (2, "bob",    md5("letmein"),      "Bob Martinez",  "bob@fintechdemo.local",    "user",  1),
        (3, "carol",  md5("sunshine99"),   "Carol Williams","carol@fintechdemo.local",  "user",  1),
        (4, "admin",  md5("Admin@2024!"),  "System Admin",  "admin@fintechdemo.local",  "admin", 1),
        (5, "dave",   md5("dave1234"),     "Dave Johnson",  "dave@fintechdemo.local",   "user",  0),  # inactive
    ]

    cur.executemany("""
        INSERT OR REPLACE INTO users (id, username, password, full_name, email, role, is_active)
        VALUES (?, ?, ?, ?, ?, ?, ?)
    """, users)

    # ------------------------------------------------------------------
    # FAKE ACCOUNTS
    # ------------------------------------------------------------------
    accounts = [
        ("ACC001", 1, "checking",  2450.75,   "USD", 0),
        ("ACC002", 2, "checking",  8120.50,   "USD", 0),
        ("ACC003", 3, "savings",   1200.00,   "USD", 0),
        ("ACC004", 4, "checking",  50000.00,  "USD", 0),  # admin account
        ("ACC005", 2, "savings",   3340.00,   "USD", 0),  # bob has 2 accounts
        ("ACC006", 5, "checking",  980.25,    "USD", 1),  # dave - frozen
    ]

    cur.executemany("""
        INSERT OR REPLACE INTO accounts (id, user_id, account_type, balance, currency, is_frozen)
        VALUES (?, ?, ?, ?, ?, ?)
    """, accounts)

    # ------------------------------------------------------------------
    # FAKE TRANSACTIONS
    # ------------------------------------------------------------------
    transactions = [
        (None,    "ACC001", 2450.75, "Initial deposit",           "deposit",    "completed", 1),
        ("ACC001","ACC002",  150.00, "Rent split - March",        "transfer",   "completed", 1),
        ("ACC002","ACC001",   50.00, "Coffee reimbursement",      "transfer",   "completed", 2),
        (None,    "ACC002", 8000.00, "Salary deposit",            "deposit",    "completed", 2),
        ("ACC001", None,    100.00,  "ATM withdrawal",            "withdrawal", "completed", 1),
        (None,    "ACC003", 1200.00, "Initial deposit",           "deposit",    "completed", 3),
        ("ACC003","ACC001",   75.00, "Lunch payment",             "transfer",   "completed", 3),
        (None,    "ACC004",50000.00, "Admin reserve fund",        "deposit",    "completed", 4),
        ("ACC002","ACC005",  500.00, "Personal savings transfer", "transfer",   "completed", 2),
        ("ACC001","ACC003",   25.00, "Birthday gift",             "transfer",   "completed", 1),
    ]

    cur.executemany("""
        INSERT INTO transactions (from_account, to_account, amount, description, tx_type, status, initiated_by)
        VALUES (?, ?, ?, ?, ?, ?, ?)
    """, transactions)

    conn.commit()
    conn.close()
    print(f"[seed] Database seeded at {DB_PATH}")
    print("[seed] Demo users:")
    print("  alice     / password123  (user,  ACC001)")
    print("  bob       / letmein      (user,  ACC002, ACC005)")
    print("  carol     / sunshine99   (user,  ACC003)")
    print("  admin     / Admin@2024!  (admin, ACC004)")
    print("  dave      / dave1234     (user,  ACC006 - inactive)")


if __name__ == "__main__":
    seed()
