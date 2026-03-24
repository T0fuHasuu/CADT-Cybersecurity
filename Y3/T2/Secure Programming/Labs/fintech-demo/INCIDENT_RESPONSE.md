# Incident Response Narrative
## What the Logs Show During the Attack Chain

**Scenario:** An attacker with no prior credentials exploits all three vulnerabilities
in a single session against the vulnerable version of FintechDemo.

**Log file:** `logs/vulnerable.log`
**Format:** NDJSON — one JSON object per line, UTC timestamps

---

## Timeline of Events

### T+00:00 — Attacker arrives at the login page

No log event generated (GET requests to the login form are not logged — acceptable
for a public-facing form, but access logs at the reverse proxy level would record it).

---

### T+00:12 — SQL Injection attempt

The attacker submits `admin'--` as the username.

The heuristic in `auth.py` detects the `--` character sequence and fires before
the query executes:

```json
{
  "timestamp": "2024-03-15T09:14:22.341Z",
  "level": "CRITICAL",
  "app": "fintechdemo",
  "app_version": "1.0.0-vulnerable",
  "event_type": "security.sqli_attempt",
  "user": "anonymous",
  "ip": "192.168.1.45",
  "message": "Possible SQL injection in login form",
  "detail": {
    "raw_input": "admin'--"
  }
}
```

**Wazuh detection:** Rule 100001 triggers on `level: CRITICAL` → **Alert Level 12**.
This alone should page an on-call analyst. In the vulnerable app, this log is
generated but the request is **not blocked** — the SQL injection succeeds anyway.

---

### T+00:12 — Successful login as admin (via SQL injection)

The injected query returns the admin record. A normal login_success event fires:

```json
{
  "timestamp": "2024-03-15T09:14:22.489Z",
  "level": "INFO",
  "app": "fintechdemo",
  "app_version": "1.0.0-vulnerable",
  "event_type": "auth.login_success",
  "user": "admin",
  "ip": "192.168.1.45",
  "message": "Successful login for 'admin'",
  "detail": {}
}
```

**Analyst note:** Taken alone, this looks like a successful admin login. **Combined
with the preceding CRITICAL SQLi event from the same IP**, this is clearly a
successful authentication bypass. The 147ms gap between events (22.341 → 22.489)
is consistent with a single form submission — not a legitimate login.

**Wazuh correlation opportunity:** Create a rule that fires when
`auth.login_success` follows `security.sqli_attempt` from the same IP within 5 seconds.

---

### T+00:35 — IDOR enumeration begins

The attacker navigates to several account pages in rapid succession:

```json
{
  "timestamp": "2024-03-15T09:14:45.112Z",
  "level": "INFO",
  "app": "fintechdemo",
  "event_type": "account.viewed",
  "user": "admin",
  "ip": "192.168.1.45",
  "message": "Account ACC001 viewed by 'admin' (owner: 'alice')",
  "detail": { "account_id": "ACC001", "owner": "alice", "suspicious": false }
}
```

```json
{
  "timestamp": "2024-03-15T09:14:46.034Z",
  "level": "INFO",
  "app": "fintechdemo",
  "event_type": "account.viewed",
  "user": "admin",
  "ip": "192.168.1.45",
  "message": "Account ACC002 viewed by 'admin' (owner: 'bob')",
  "detail": { "account_id": "ACC002", "owner": "bob", "suspicious": false }
}
```

**Analyst note:** Since the attacker is using the compromised *admin* session,
`suspicious: false` — admin is allowed to see all accounts. The enumeration
velocity (6 accounts in under 2 seconds) is behaviorally anomalous but would
require a velocity-based rule to trigger. This is a detection gap.

**If the attacker used alice's credentials instead** (skipping VULN 1), the IDOR
access to ACC004 would generate:

```json
{
  "timestamp": "2024-03-15T09:14:47.891Z",
  "level": "WARNING",
  "app": "fintechdemo",
  "event_type": "account.viewed",
  "user": "alice",
  "ip": "192.168.1.45",
  "message": "Account ACC004 viewed by 'alice' (owner: 'admin')",
  "detail": { "account_id": "ACC004", "owner": "admin", "suspicious": true }
}
```

**Wazuh detection:** Rule 100002 triggers on `suspicious: true` → **Alert Level 8**.

---

### T+01:10 — Unauthorized transfer executed

The attacker submits the manipulated transfer form:
`from_account=ACC004, to_account=ACC001, amount=10000`

Because VULN 3 has no ownership check, the transfer completes. The log records
`authorized=True` — a false positive in the vulnerable app because we never
actually checked authorization:

```json
{
  "timestamp": "2024-03-15T09:15:32.209Z",
  "level": "INFO",
  "app": "fintechdemo",
  "app_version": "1.0.0-vulnerable",
  "event_type": "transfer.completed",
  "user": "admin",
  "ip": "192.168.1.45",
  "message": "Transfer $10000.00 from ACC004 to ACC001 by 'admin' (authorized=True)",
  "detail": {
    "from_account": "ACC004",
    "to_account": "ACC001",
    "amount": 10000.0,
    "authorized": true
  }
}
```

**Analyst note:** This log event appears completely legitimate in isolation.
The admin is shown initiating a transfer from their own account (ACC004). Without
correlating against the earlier SQLi and IDOR events, this is invisible.

**Detection gap:** The vulnerable app logs `authorized=True` even though no check
was performed. In the hardened version, this code path logs `authorized=False`
and fires a CRITICAL event before aborting. The difference in logging behaviour
between versions is a key teaching point.

---

## Full Incident Timeline (Compressed View)

```
09:14:22.341  CRITICAL  security.sqli_attempt       ip=192.168.1.45  input=admin'--
09:14:22.489  INFO      auth.login_success           user=admin       ip=192.168.1.45
09:14:45.112  INFO      account.viewed               suspicious=false  (admin view of ACC001)
09:14:46.034  INFO      account.viewed               suspicious=false  (admin view of ACC002)
09:14:46.891  INFO      account.viewed               suspicious=false  (admin view of ACC003)
09:14:47.344  INFO      account.viewed               suspicious=false  (admin view of ACC004)
09:14:47.789  INFO      account.viewed               suspicious=false  (admin view of ACC005)
09:15:32.209  INFO      transfer.completed           ACC004→ACC001  $10,000  authorized=True
```

**Red flags visible in hindsight:**
1. CRITICAL SQLi event at T+0 from the same IP as later activity
2. Login success 148ms after SQLi event from the same IP
3. 6 account views in 2.2 seconds (automated enumeration pattern)
4. $10,000 transfer immediately after enumeration

**Missing from the logs:**
- The transfer authorization was never verified — the log says `authorized=True`
  but that field was set unconditionally in the vulnerable code
- No record that the `from_account` value came from form manipulation

---

## What the Hardened Logs Look Like for the Same Attacks

### Attack 1 — SQLi attempt (blocked)

```json
{ "event_type": "security.sqli_attempt", "level": "CRITICAL",
  "detail": { "raw_input": "admin'--" } }

{ "event_type": "auth.login_failure", "level": "WARNING",
  "detail": { "reason": "bad_credentials" } }
```

No `login_success` follows. The attack fails at step 1.

### Attack 2 — IDOR attempt (blocked)

```json
{
  "event_type": "security.idor_attempt",
  "level": "WARNING",
  "user": "alice",
  "message": "User 'alice' tried to access account ACC004 owned by 'admin'",
  "detail": { "target_account": "ACC004", "owner": "admin" }
}
```

### Attack 3 — Unauthorized transfer (blocked)

```json
{
  "event_type": "transfer.unauthorized",
  "level": "CRITICAL",
  "user": "alice",
  "message": "Transfer $10000.00 from ACC004 to ACC001 by 'alice' (authorized=False)",
  "detail": {
    "from_account": "ACC004",
    "to_account": "ACC001",
    "amount": 10000.0,
    "authorized": false
  }
}

{
  "event_type": "security.unauthorized_transfer_attempt",
  "level": "WARNING",
  "user": "alice",
  "message": "'alice' attempted to transfer from account ACC004 (owned by user #4)",
  "detail": {
    "from_account": "ACC004",
    "to_account": "ACC001",
    "amount": "10000"
  }
}
```

**Wazuh fires Rule 100001 (CRITICAL)** — on-call analyst alerted within seconds.

---

## Incident Response Playbook (Post-Detection)

### Immediate (0–15 minutes)

1. **Isolate:** If Wazuh fires on `transfer.unauthorized` CRITICAL, immediately
   review the session associated with the event. Consider invalidating the session.
2. **Assess scope:** Pull all log events from the same IP address in the past hour.
   Look for the SQLi → login_success pattern.
3. **Freeze affected accounts:** If a transfer completed before detection, freeze
   both source and destination accounts pending investigation.

### Short-term (15 min – 2 hours)

4. **Preserve evidence:** Copy and hash the log files before any rotation.
5. **Trace the chain:** Use the `user` and `ip` fields to reconstruct the full
   event sequence from first request to transfer completion.
6. **Identify data exposure:** Which accounts were viewed by the compromised
   session? (Query `account.viewed` events with that user/IP.)
7. **Notify affected account holders:** Regulatory requirement in most jurisdictions
   within 72 hours if PII or financial data was accessed.

### Long-term (24–48 hours)

8. **Deploy patched version:** The hardened build was ready — deploy it.
9. **Rotate secrets:** Change `SECRET_KEY` — attacker may have extracted it from
   the `debug=True` stack traces in the vulnerable version.
10. **Password reset:** If MD5 hashes were exposed via the admin panel, all
    passwords should be considered compromised — force reset for all users.
11. **Post-mortem:** Document root cause, detection time, response time, and
    what controls would have prevented or detected the attack earlier.

---

## Key Lesson for the Classroom

> In the vulnerable version, the three attacks generated logs — but the logs
> told an incomplete story because the code itself didn't know an attack was
> happening (VULN 3 logged `authorized=True` for an unauthorized transfer).
>
> Logs are only as useful as the code generating them. Security-aware logging
> means instrumenting not just *what happened*, but *whether it was permitted*.
> The hardened version's logs contain the word `authorized=False` and
> `event_type: transfer.unauthorized` — making automated detection trivial.
>
> **Write your logs for your future SIEM analyst, not just for debugging.**
