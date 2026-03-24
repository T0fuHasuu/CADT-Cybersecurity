#!/usr/bin/env python3
"""
seed.py - Populate the database with fake demo data.
HARDENED VERSION: passwords stored with bcrypt (work factor 12 + per-user salt).

FOR EDUCATIONAL USE ONLY - all users, balances, and transactions are fictional.
"""

import sqlite3
import os
import sys

try:
    import bcrypt
except ImportError:
    print("[seed] ERROR: bcrypt not installed. Run: pip install bcrypt")
    sys.exit(1)

DB_PATH    = os.environ.get("DB_PATH", "bank.db")
SCHEMA_PATH = os.path.join(os.path.dirname(__file__), "schema.sql")


def bcrypt_hash(password: str) -> str:
    """
    SECURE FIX for Vulnerability 1 (Insecure Password Storage):
    bcrypt automatically generates a unique 128-bit salt per password,
    then embeds the salt in the returned hash string.
    Work factor 12 means ~250ms per verification — fast for legitimate
    users, brutally slow for attackers trying millions of candidates.

    Compare with MD5 (vulnerable version):
      MD5 throughput on GPU:   ~10 billion hashes/second
      bcrypt w=12 throughput:  ~100 hashes/second
    """
    salt = bcrypt.gensalt(rounds=12)
    return bcrypt.hashpw(password.encode(), salt).decode()


def seed():
    conn = sqlite3.connect(DB_PATH)
    conn.row_factory = sqlite3.Row
    cur = conn.cursor()

    with open(SCHEMA_PATH, "r") as f:
        conn.executescript(f.read())

    print("[seed] Hashing passwords with bcrypt (this takes a few seconds)…")

    users = [
        (1, "alice", bcrypt_hash("password123"), "Alice Chen",     "alice@fintechdemo.local",  "user",  1),
        (2, "bob",   bcrypt_hash("letmein"),      "Bob Martinez",   "bob@fintechdemo.local",    "user",  1),
        (3, "carol", bcrypt_hash("sunshine99"),   "Carol Williams", "carol@fintechdemo.local",  "user",  1),
        (4, "admin", bcrypt_hash("Admin@2024!"),  "System Admin",   "admin@fintechdemo.local",  "admin", 1),
        (5, "dave",  bcrypt_hash("dave1234"),     "Dave Johnson",   "dave@fintechdemo.local",   "user",  0),
    ]

    cur.executemany("""
        INSERT OR REPLACE INTO users
            (id, username, password, full_name, email, role, is_active)
        VALUES (?, ?, ?, ?, ?, ?, ?)
    """, users)

    accounts = [
        ("ACC001", 1, "checking",  2450.75,  "USD", 0),
        ("ACC002", 2, "checking",  8120.50,  "USD", 0),
        ("ACC003", 3, "savings",   1200.00,  "USD", 0),
        ("ACC004", 4, "checking",  50000.00, "USD", 0),
        ("ACC005", 2, "savings",   3340.00,  "USD", 0),
        ("ACC006", 5, "checking",  980.25,   "USD", 1),
    ]

    cur.executemany("""
        INSERT OR REPLACE INTO accounts
            (id, user_id, account_type, balance, currency, is_frozen)
        VALUES (?, ?, ?, ?, ?, ?)
    """, accounts)

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
        INSERT INTO transactions
            (from_account, to_account, amount, description, tx_type, status, initiated_by)
        VALUES (?, ?, ?, ?, ?, ?, ?)
    """, transactions)

    conn.commit()
    conn.close()
    print(f"[seed] Database seeded at {DB_PATH}")
    print("[seed] Demo users (hardened — bcrypt hashes):")
    print("  alice  / password123")
    print("  bob    / letmein")
    print("  carol  / sunshine99")
    print("  admin  / Admin@2024!")


if __name__ == "__main__":
    seed()
