# FintechDemo Banking Lab

**A complete offline-only mock banking portal for secure programming education.**

> ⚠️ **FOR CLASSROOM USE ONLY.**
> All users, balances, and transactions are entirely fictional.
> This application contains **intentional security vulnerabilities**.
> Do not deploy to any internet-facing or production environment.

---

## What Is This?

FintechDemo is a full-stack banking web application built to teach secure programming
through hands-on demonstration. It runs two versions side by side:

| Version | URL | Purpose |
|---------|-----|---------|
| **Vulnerable** | http://localhost:5000 | Contains 3 intentional flaws for demonstration |
| **Hardened**   | http://localhost:5001 | Same app with each flaw fixed and explained |

The demo tells a single attack story — from login bypass, through account
enumeration, to an unauthorized money transfer — and then shows exactly what
the hardened code does differently to stop each step.

---

## Project Structure

```
fintech-demo/
├── docker-compose.yml         ← Start everything with one command
├── logs/                      ← Shared log directory (Wazuh-ingestable JSON)
│
├── vulnerable/
│   ├── app.py                 ← Flask app factory (hardcoded SECRET_KEY)
│   ├── auth.py                ← Login routes  [VULN 1: SQL Injection]
│   ├── accounts.py            ← Account routes [VULN 2: IDOR]
│   ├── transfers.py           ← Transfer routes [VULN 3: Missing AuthZ]
│   ├── admin.py               ← Admin panel
│   ├── database.py            ← DB helpers (includes execute_unsafe)
│   ├── logger.py              ← Structured JSON logger
│   ├── seed.py                ← Seed DB with fake data (MD5 passwords)
│   ├── schema.sql             ← SQLite schema
│   ├── requirements.txt
│   ├── Dockerfile
│   └── templates/             ← Jinja2 HTML templates
│
├── hardened/
│   ├── app.py                 ← Flask app factory (env SECRET_KEY, security headers)
│   ├── auth.py                ← FIX 1: parameterized queries + bcrypt
│   ├── accounts.py            ← FIX 2: ownership check on every account fetch
│   ├── transfers.py           ← FIX 3: server-side authorization before transfer
│   ├── admin.py               ← Admin panel (unchanged logic, benefits from FIX 1)
│   ├── database.py            ← DB helpers (execute_unsafe removed)
│   ├── logger.py              ← Same logger, updated app_version
│   ├── seed.py                ← Seed DB with fake data (bcrypt passwords)
│   ├── schema.sql             ← Same schema
│   ├── requirements.txt       ← + bcrypt
│   ├── Dockerfile             ← Runs as non-root user
│   └── templates/
```

---

## Quick Start (Docker — Recommended)

### Prerequisites

- Docker Desktop 4.x or Docker Engine 24+
- Docker Compose v2 (`docker compose` command)
- Ports 5000 and 5001 free on your machine

### Start both apps

```bash
git clone <repo> fintech-demo
cd fintech-demo

# Build and start both containers
docker compose up --build

# Or run in background
docker compose up --build -d
```

The first run takes ~2 minutes (building images, installing packages, seeding databases).

### Access the apps

- **Vulnerable:** http://localhost:5000
- **Hardened:**   http://localhost:5001

### Stop

```bash
docker compose down
```

---

## Manual Setup (without Docker)

### Prerequisites

- Python 3.10+
- pip

### Vulnerable app

```bash
cd vulnerable
pip install -r requirements.txt
python seed.py          # creates bank.db and populates fake data
python app.py           # starts on http://localhost:5000
```

### Hardened app

```bash
cd hardened
pip install -r requirements.txt
python seed.py          # bcrypt hashing — takes ~10 seconds

# Generate a secure secret key
export SECRET_KEY=$(python -c "import secrets; print(secrets.token_hex(32))")

python app.py           # starts on http://localhost:5001
```

---

## Demo User Accounts

| Username | Password     | Role  | Account(s)       | Notes          |
|----------|--------------|-------|------------------|----------------|
| alice    | password123  | user  | ACC001           | Primary demo user |
| bob      | letmein      | user  | ACC002, ACC005   | Has two accounts |
| carol    | sunshine99   | user  | ACC003           |                |
| admin    | Admin@2024!  | admin | ACC004 ($50,000) | Target for exploit |
| dave     | dave1234     | user  | ACC006           | Inactive account |

---

## Resetting the Database

### Docker

```bash
# Stop containers, remove volumes (clears all DB data), restart
docker compose down -v
docker compose up --build
```

### Manual

```bash
rm -f vulnerable/bank.db
python vulnerable/seed.py

rm -f hardened/bank.db
python hardened/seed.py
```

---

## Viewing Logs

Both apps write structured JSON logs (one JSON object per line) to `./logs/`.

```bash
# Tail the vulnerable app log
tail -f logs/vulnerable.log | python -m json.tool

# Filter for security events only
cat logs/vulnerable.log | grep '"level": "WARNING"' | python -m json.tool

# Filter for critical events (unauthorized transfers, SQLi attempts)
cat logs/vulnerable.log | grep '"level": "CRITICAL"'
```

### Wazuh Integration

Point a Wazuh agent `localfile` monitor at `./logs/` with `format: json`:

```xml
<localfile>
  <log_format>json</log_format>
  <location>/path/to/fintech-demo/logs/vulnerable.log</location>
</localfile>
```

Key `event_type` values to write Wazuh rules for:

| event_type | level | Meaning |
|------------|-------|---------|
| `auth.login_failure` | WARNING | Failed login — potential brute force |
| `security.sqli_attempt` | CRITICAL | SQL injection chars detected in login |
| `account.viewed` + `suspicious: true` | WARNING | IDOR — user viewed another user's account |
| `transfer.unauthorized` | CRITICAL | Unauthorized transfer attempt (VULN 3) |
| `security.unauthorized_transfer_attempt` | WARNING | Same, with detail payload |
| `security.idor_attempt` | WARNING | IDOR blocked (hardened version) |

---

## Demo Vulnerability Payloads

### VULN 1 — SQL Injection (Login Bypass)

```
Username: admin'--
Password: anything
```

Result (vulnerable): Logged in as admin. Session role = admin.

Result (hardened): Login fails. Rate limit counter incremented.

### VULN 2 — IDOR (Account Enumeration)

While logged in as alice, visit:
```
http://localhost:5000/account/ACC004
```

Result (vulnerable): Alice sees admin's $50,000 account balance and transactions.

Result (hardened): 403 Forbidden. Wazuh log entry with `event_type: security.idor_attempt`.

### VULN 3 — Missing Authorization (Unauthorized Transfer)

While logged in as alice, open the transfer form and change `from_account` to `ACC004` via browser DevTools, or use curl:

```bash
# Get alice's session cookie from browser DevTools → Application → Cookies
curl -X POST http://localhost:5000/transfer \
  -H "Content-Type: application/x-www-form-urlencoded" \
  -b "session=<ALICE_SESSION_COOKIE>" \
  -d "from_account=ACC004&to_account=ACC001&amount=10000&description=test"
```

Result (vulnerable): $10,000 moved from admin's account to alice's account.

Result (hardened): 403 Forbidden. Log entry with `event_type: transfer.unauthorized`.
