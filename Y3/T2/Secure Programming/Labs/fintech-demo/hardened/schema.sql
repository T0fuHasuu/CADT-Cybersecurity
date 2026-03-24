-- =============================================================
-- FintechDemo Banking App - Database Schema
-- FOR EDUCATIONAL USE ONLY - Contains intentional vulnerabilities
-- =============================================================

CREATE TABLE IF NOT EXISTS users (
    id          INTEGER PRIMARY KEY AUTOINCREMENT,
    username    TEXT UNIQUE NOT NULL,
    password    TEXT NOT NULL,          -- VULN: MD5 hash (insecure)
    full_name   TEXT NOT NULL,
    email       TEXT UNIQUE NOT NULL,
    role        TEXT NOT NULL DEFAULT 'user',  -- 'user' or 'admin'
    is_active   INTEGER NOT NULL DEFAULT 1,
    created_at  DATETIME DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE IF NOT EXISTS accounts (
    id          TEXT PRIMARY KEY,       -- e.g. ACC001
    user_id     INTEGER NOT NULL,
    account_type TEXT NOT NULL DEFAULT 'checking',
    balance     REAL NOT NULL DEFAULT 0.0,
    currency    TEXT NOT NULL DEFAULT 'USD',
    is_frozen   INTEGER NOT NULL DEFAULT 0,
    created_at  DATETIME DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (user_id) REFERENCES users(id)
);

CREATE TABLE IF NOT EXISTS transactions (
    id              INTEGER PRIMARY KEY AUTOINCREMENT,
    from_account    TEXT,
    to_account      TEXT,
    amount          REAL NOT NULL,
    description     TEXT,
    tx_type         TEXT NOT NULL,   -- 'transfer', 'deposit', 'withdrawal'
    status          TEXT NOT NULL DEFAULT 'completed',
    initiated_by    INTEGER,         -- user_id who triggered it
    created_at      DATETIME DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (from_account) REFERENCES accounts(id),
    FOREIGN KEY (to_account)   REFERENCES accounts(id),
    FOREIGN KEY (initiated_by) REFERENCES users(id)
);

CREATE TABLE IF NOT EXISTS audit_log (
    id          INTEGER PRIMARY KEY AUTOINCREMENT,
    user_id     INTEGER,
    action      TEXT NOT NULL,
    detail      TEXT,
    ip_address  TEXT,
    created_at  DATETIME DEFAULT CURRENT_TIMESTAMP
);
