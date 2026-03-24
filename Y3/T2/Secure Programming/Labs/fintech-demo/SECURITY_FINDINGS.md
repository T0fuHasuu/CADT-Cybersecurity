# Security Findings Report
## FintechDemo Banking Lab — Vulnerability Analysis & Remediation

**Classification:** Educational Demo Only
**App Version:** 1.0.0-vulnerable → 2.0.0-hardened
**Assessment Type:** Intentional vulnerability demonstration
**Date:** 2024

---

## Executive Summary

The vulnerable version of FintechDemo contains three intentionally introduced security
flaws that chain together into a complete attack path: an attacker can bypass
authentication via SQL injection, enumerate account balances via IDOR, and then
execute an unauthorized money transfer against any discovered account.

Each flaw maps directly to the OWASP Top 10 (2021) and has a well-understood,
minimal-effort fix that is demonstrated in the hardened version.

---

## Vulnerability 1 — SQL Injection in Login Form

**CWE:**          CWE-89 (Improper Neutralization of Special Elements in SQL Commands)
**OWASP 2021:**   A03 — Injection
**CVSS v3 Base:** 9.8 (Critical) — Network / Low complexity / No privileges / No interaction
**File:**         `vulnerable/auth.py`, lines 71–82
**Function:**     `login()`

### Description

The login route builds its SQL query by directly embedding form inputs into a Python
f-string. There is no escaping, no parameterization, and no use of a query-builder.
The resulting SQL string is passed to `execute_unsafe()`, which calls
`conn.execute()` on the raw string.

```python
# VULNERABLE CODE (auth.py line 78)
unsafe_sql = (
    f"SELECT * FROM users "
    f"WHERE username='{username}' "
    f"AND password='{password_hash}' "
    f"AND is_active=1"
)
rows = db.execute_unsafe(unsafe_sql)
```

### Attack Payload

```
Username: admin'--
Password: anything
```

The injected string produces:

```sql
SELECT * FROM users
WHERE username='admin'--' AND password='...' AND is_active=1
```

The `--` sequence begins an SQL comment, causing the parser to discard everything
after it. The effective query becomes:

```sql
SELECT * FROM users WHERE username='admin'
```

This returns the admin user record without verifying the password at all. The
attacker receives a fully authenticated admin session.

### Impact

- Complete authentication bypass for any known username
- Admin access without credentials
- Gateway to all admin-only functionality
- Can be extended to UNION-based data exfiltration or blind SQLi

### Root Cause

Violation of the principle of **input separation**: data (user input) and
instructions (SQL syntax) are mixed in the same string. The database parser
cannot distinguish between intended structure and injected structure.

### Remediation (implemented in `hardened/auth.py`)

Replace string formatting with parameterized placeholders. The SQLite driver
substitutes `?` markers by passing the value as a separate parameter — the
value is always treated as data, never as syntax:

```python
# SECURE CODE (hardened/auth.py)
user = db.query_one(
    "SELECT * FROM users WHERE username=? AND is_active=1",
    (username,)
)
```

Note that the password check moves out of the SQL and into Python (bcrypt),
so the query only looks up by username. This separates concerns: SQL handles
lookup, Python handles credential verification.

### Additional Fix — Insecure Password Storage (CWE-916)

The vulnerable version stores passwords as unsalted MD5 hashes. MD5 was never
designed for password storage — it is a fast checksum algorithm. A modern GPU
can compute roughly 10 billion MD5 hashes per second, making rainbow-table and
brute-force attacks trivial.

**Hardened fix:** bcrypt with work factor 12. bcrypt is purpose-built for
password hashing. It generates a unique 128-bit salt per password and embeds
it in the stored hash. Work factor 12 limits throughput to roughly 100
verifications per second per GPU core, making brute force impractical.

```python
# VULNERABLE (seed.py)
hashlib.md5(password.encode()).hexdigest()   # no salt, fast, reversible with tables

# SECURE (hardened/seed.py)
bcrypt.hashpw(password.encode(), bcrypt.gensalt(rounds=12))  # salted, slow, irreversible
```

### Secure Coding Principle

> **"Never concatenate user-controlled data into a query string.
> Always use parameterized queries or prepared statements."**
> — OWASP SQL Injection Prevention Cheat Sheet

---

## Vulnerability 2 — Insecure Direct Object Reference (IDOR)

**CWE:**          CWE-639 (Authorization Bypass Through User-Controlled Key)
**OWASP 2021:**   A01 — Broken Access Control
**CVSS v3 Base:** 6.5 (Medium) — Network / Low complexity / Low privileges / No interaction
**File:**         `vulnerable/accounts.py`, lines 71–85
**Function:**     `account_detail()`

### Description

The `/account/<account_id>` endpoint fetches any account record by its primary key
and renders it to the current user — without ever checking whether the logged-in
user owns that account. The account ID is a short, predictable string (ACC001–ACC006),
making enumeration trivial.

```python
# VULNERABLE CODE (accounts.py)
account = db.query_one(
    "SELECT a.*, u.username AS owner FROM accounts a "
    "JOIN users u ON a.user_id = u.id WHERE a.id=?",
    (account_id,)
)
if not account:
    abort(404)
# Missing ownership check — any authenticated user can read any account
return render_template("account_detail.html", account=account, ...)
```

### Attack Scenario

1. Alice (legitimate user) logs in, sees her own account ACC001.
2. Alice manually navigates to `/account/ACC004`.
3. The server fetches the admin's account and renders it for Alice.
4. Alice sees the admin's balance ($50,000) and full transaction history.
5. Alice now knows a high-value target account ID to use in VULN 3.

### Impact

- Complete disclosure of balance and transaction history for all accounts
- Account IDs for use in further attacks (chained to VULN 3)
- Potential regulatory violation (financial data exposure without consent)
- Enumeration possible with a simple for-loop: `for i in range(1, 100)`

### Root Cause

The developer implemented authentication (is the user logged in?) but not
authorization (does the user have permission to access *this specific resource?*).
These are two distinct checks and both are required.

### Remediation (implemented in `hardened/accounts.py`)

After fetching the account, compare the database ownership field against the
server-managed session value. Admins are given an exception for support purposes;
all cross-user views by admins are logged.

```python
# SECURE CODE (hardened/accounts.py)
is_owner = (account["user_id"] == session["user_id"])
is_admin  = (session.get("role") == "admin")

if not is_owner and not is_admin:
    log.security_event("idor_attempt", ...)
    abort(403)
```

This check runs server-side in Python against an authoritative database value.
There is no mechanism for a client to bypass it.

### Secure Coding Principle

> **"Always perform authorization checks at the resource level,
> not just at the route level. Verify ownership for every object
> access — authentication alone is not authorization."**
> — OWASP Broken Access Control Prevention Cheat Sheet

---

## Vulnerability 3 — Missing Server-Side Authorization on Transfer

**CWE:**          CWE-862 (Missing Authorization)
**OWASP 2021:**   A01 — Broken Access Control
**CVSS v3 Base:** 8.1 (High) — Network / Low complexity / Low privileges / No interaction
**File:**         `vulnerable/transfers.py`, lines 83–100
**Function:**     `transfer()`

### Description

The transfer endpoint reads `from_account` from the POST body and executes the
transfer without verifying that the authenticated user owns that account. The UI
dropdown is pre-populated with only the user's own accounts, but this is a
client-side control only — the server never validates it.

```python
# VULNERABLE CODE (transfers.py)
from_account_id = request.form.get("from_account", "").strip()
from_account = db.query_one(
    "SELECT * FROM accounts WHERE id=?", (from_account_id,)
)
# from_account["user_id"] is available but NEVER compared to session["user_id"]
# Transfer proceeds regardless of who owns the source account
db.execute("UPDATE accounts SET balance = balance - ? WHERE id=?",
           (amount, from_account_id))
```

### Attack Scenario (the complete chain)

```
Step 1 [VULN 1]: alice logs in using: username=admin'--  password=anything
                 → Receives admin session, sees ACC004 on dashboard

  (Alternatively: alice uses her own credentials for a subtler attack)

Step 2 [VULN 2]: alice visits /account/ACC004
                 → Sees admin balance: $50,000.00

Step 3 [VULN 3]: alice opens /transfer, changes from_account via DevTools
                 (or crafts a raw HTTP POST) with from_account=ACC004

Step 4 [RESULT]: $10,000 transferred from admin's account to alice's account
                 Server logs show alice's user_id as initiator — admin loses money
```

Curl command to demonstrate (no browser needed):

```bash
curl -s -X POST http://localhost:5000/transfer \
  -H "Content-Type: application/x-www-form-urlencoded" \
  -b "session=<ALICE_SESSION_COOKIE>" \
  -d "from_account=ACC004&to_account=ACC001&amount=10000&description=nothing"
```

### Impact

- Unauthorized financial transfers from any account to any account
- Direct financial loss (in a real system)
- Non-repudiation broken: alice's session is the logged initiator, yet admin
  loses money — forensic attribution is complicated
- Could drain all accounts in an automated loop

### Root Cause

Client-side enforcement (pre-populated dropdown, hidden fields) is not a security
control. Any data in an HTTP request body can be modified by the client before
sending. The server is the only trust boundary and must validate all inputs.

### Remediation (implemented in `hardened/transfers.py`)

After fetching the source account from the database, verify ownership before
proceeding. Log the unauthorized attempt with enough detail for forensics, then
return 403.

```python
# SECURE CODE (hardened/transfers.py)
if from_account["user_id"] != user_id:
    log.transfer_initiated(username, from_account_id, to_account_id,
                           float(amount), ip, authorized=False)
    log.security_event("unauthorized_transfer_attempt", ...,
                       detail={"from_account": from_account_id,
                               "amount": str(amount)})
    abort(403)
```

Additional hardening in the transfer endpoint:
- Amount parsed as `Decimal` (not `float`) — avoids IEEE 754 rounding issues
  that could allow fractional penny theft at scale
- Both balance updates execute inside a single database transaction, preventing
  partial transfers if an error occurs mid-operation
- Description field truncated server-side to 120 characters regardless of input

### Secure Coding Principle

> **"Never trust client-supplied identifiers for authorization decisions.
> Every server-side operation that modifies data must verify that the
> authenticated principal has permission to perform that specific action
> on that specific resource."**
> — OWASP Authorization Cheat Sheet

---

## Bonus Vulnerability — Hardcoded Secret Key

**CWE:**          CWE-798 (Use of Hard-Coded Credentials)
**OWASP 2021:**   A02 — Cryptographic Failures
**File:**         `vulnerable/app.py`, line 22

```python
# VULNERABLE
app.config["SECRET_KEY"] = "supersecret123"
```

Flask uses the secret key to cryptographically sign session cookies (HMAC-SHA1).
Anyone who knows the key can forge arbitrary session cookies — including
`{"user_id": 4, "role": "admin"}` — without ever authenticating.

If the key is committed to source control, every person with repository access
can become any user in the system.

**Fix (hardened/app.py):**

```python
# SECURE
secret_key = os.environ.get("SECRET_KEY", "")
_validate_secret_key(secret_key)    # rejects keys < 32 chars or known-weak values
app.config["SECRET_KEY"] = secret_key
```

The `_validate_secret_key()` function refuses to start the application if the
key is too short, empty, or matches a known-weak string. This makes misconfiguration
a startup error rather than a silent security gap.

---

## Vulnerability Interaction Map (Attack Chain)

```
[Attacker]
    │
    ▼
┌─────────────────────────────────────────────────────┐
│  STEP 1: VULN 1 — SQL Injection                     │
│  Login with: admin'--  (any password)               │
│  Result: admin session cookie                       │
└──────────────────────┬──────────────────────────────┘
                       │ authenticated as admin
                       ▼
┌─────────────────────────────────────────────────────┐
│  STEP 2: VULN 2 — IDOR                              │
│  GET /account/ACC001 → ACC006 (enumeration)         │
│  Result: all balances + account IDs revealed        │
│  Identify high-value target: ACC004 ($50,000)       │
└──────────────────────┬──────────────────────────────┘
                       │ knows target account ID
                       ▼
┌─────────────────────────────────────────────────────┐
│  STEP 3: VULN 3 — Missing Authorization             │
│  POST /transfer from_account=ACC004                 │
│         to_account=ACC001  amount=10000             │
│  Result: $10,000 stolen from admin's account        │
└─────────────────────────────────────────────────────┘

OR (without VULN 1 — subtler attack using own credentials):

[Attacker with valid user account]
    │
    ├── VULN 2: enumerate other accounts → find high-value target
    │
    └── VULN 3: transfer from target account → own account
```

---

## Threat Model Summary

**Assets:**
- User account balances (financial data)
- Authentication credentials (username/password hashes)
- Transaction history (privacy-sensitive)
- Admin access and all associated privileges

**Threat Actors:**
- Authenticated regular user seeking financial gain
- External attacker who has obtained one regular account (phishing, credential stuffing)

**Attack Surface:**
- `/login` — unauthenticated, accepts arbitrary form input
- `/account/<id>` — authenticated but no object-level authorization
- `/transfer` — authenticated but no resource-level authorization

**Trust Boundaries:**
- HTTP request body: **untrusted** — must always be validated server-side
- Session cookie: **trusted after verification** — but only if the secret key is secret
- Database: **trusted** — the authoritative source for ownership decisions

**Residual Risk in Hardened Version:**
- CSRF on transfer endpoint (no CSRF token implemented — noted in DevSecOps checklist)
- No account lockout on brute force (in-memory rate limit only — resets on restart)
- No TLS in lab environment (acceptable for isolated classroom use)
