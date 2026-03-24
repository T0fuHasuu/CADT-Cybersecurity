# DevSecOps Security Checklist
## FintechDemo Banking Lab

**Purpose:** Checklist for integrating security into the development lifecycle.
Each item maps to a tool or technique suitable for a classroom or small team pipeline.

---

## 1. Static Application Security Testing (SAST)

SAST tools analyze source code without executing it, flagging known-dangerous patterns.

### Tool: Bandit (Python SAST)

```bash
# Install
pip install bandit

# Run against vulnerable version
bandit -r vulnerable/ -f txt -o reports/bandit-vulnerable.txt

# Run against hardened version (should have fewer/no findings)
bandit -r hardened/  -f txt -o reports/bandit-hardened.txt
```

**What Bandit detects in the vulnerable app:**

| Severity | Test ID  | Issue                                     | File              |
|----------|----------|-------------------------------------------|-------------------|
| HIGH     | B608     | SQL injection via string formatting       | vulnerable/auth.py |
| MEDIUM   | B324     | MD5 used for password hashing             | vulnerable/seed.py |
| LOW      | B105     | Hardcoded password / secret key           | vulnerable/app.py  |

**Expected outcome on hardened app:** B608 and B324 resolved; B105 resolved
because SECRET_KEY is loaded from environment.

### Tool: Semgrep (multi-language, rule-based)

```bash
# Install
pip install semgrep

# Run OWASP top-10 ruleset
semgrep --config=p/owasp-top-ten vulnerable/

# Run Flask-specific security rules
semgrep --config=p/flask vulnerable/
```

**Key rules triggered on vulnerable app:**
- `python.flask.security.injection.raw-query-format-string` (VULN 1)
- `python.cryptography.security.insecure-hash-algorithms.md5` (password hashing)
- `python.flask.security.audit.hardcoded-secret` (SECRET_KEY)

---

## 2. Dependency / SCA Scanning

Software Composition Analysis (SCA) checks third-party packages for known CVEs.

### Tool: pip-audit

```bash
# Install
pip install pip-audit

# Audit vulnerable app dependencies
pip-audit -r vulnerable/requirements.txt -f json -o reports/sca-vulnerable.json

# Audit hardened app dependencies
pip-audit -r hardened/requirements.txt  -f json -o reports/sca-hardened.json
```

### Tool: Safety

```bash
pip install safety
safety check -r vulnerable/requirements.txt
```

### Tool: Trivy (container scanning)

```bash
# Scan the built Docker images
trivy image fintech-demo-vulnerable:latest --format table
trivy image fintech-demo-hardened:latest  --format table

# Also scan the filesystem for dependency issues
trivy fs vulnerable/ --scanners vuln,secret,misconfig
```

**What to look for:**
- CVEs in Flask, Werkzeug, Jinja2 — these have had injection-related CVEs
- Python base image CVEs (use `python:3.11-slim` to reduce attack surface)
- Outdated bcrypt (check for timing-safe comparison guarantees)

### Dependency Pinning Checklist

```
[ ] All dependencies pinned to exact versions in requirements.txt
[ ] requirements.txt committed to source control
[ ] Automated weekly dependency update check (Dependabot, Renovate, or pip-audit in CI)
[ ] No dev dependencies included in production requirements.txt
```

---

## 3. Secret / Credential Scanning

Prevent secrets from being committed to source control.

### Tool: truffleHog

```bash
pip install trufflehog
trufflehog filesystem . --exclude-paths=".git" --json > reports/secrets.json
```

### Tool: detect-secrets (pre-commit hook)

```bash
pip install detect-secrets

# Generate baseline (acknowledges known non-secrets)
detect-secrets scan > .secrets.baseline

# Run scan
detect-secrets scan --baseline .secrets.baseline

# Install as pre-commit hook
pre-commit install
```

Sample `.pre-commit-config.yaml`:

```yaml
repos:
  - repo: https://github.com/Yelp/detect-secrets
    rev: v1.4.0
    hooks:
      - id: detect-secrets
        args: ['--baseline', '.secrets.baseline']
```

### Tool: git-secrets (AWS-focused but general)

```bash
brew install git-secrets   # macOS
git secrets --install      # install hooks
git secrets --scan         # scan entire repo history
```

### What to search for in this codebase

```
grep -r "SECRET_KEY\s*=\s*['\"]" .          # Hardcoded Flask secret
grep -r "password\s*=\s*['\"]" .             # Hardcoded passwords
grep -r "md5\|sha1" . --include="*.py"       # Weak hash functions
grep -r "execute_unsafe\|format.*WHERE" .    # SQL injection patterns
grep -r "0\.0\.0\.0" . --include="*.py"      # Binding to all interfaces
```

### Secret Scanning Checklist

```
[ ] .env files added to .gitignore
[ ] No secrets in docker-compose.yml (use Docker secrets or env files)
[ ] SECRET_KEY generated per-environment (not shared between dev/prod)
[ ] Database passwords (if any) not hardcoded
[ ] Pre-commit hook installed to block accidental secret commits
[ ] Secret rotation procedure documented
[ ] All developers briefed: never commit credentials, even to private repos
```

---

## 4. Logging Review

### Log Coverage Checklist

Verify the following events are logged with structured fields:

```
[x] Successful login          — event_type: auth.login_success
[x] Failed login              — event_type: auth.login_failure  (with reason)
[x] Logout                    — event_type: auth.logout
[x] SQLi attempt detected     — event_type: security.sqli_attempt
[x] Account viewed            — event_type: account.viewed  (flags suspicious=true for IDOR)
[x] Transfer completed        — event_type: transfer.completed
[x] Unauthorized transfer     — event_type: transfer.unauthorized  (hardened only)
[x] Admin panel access        — event_type: admin.action
[x] Unauthorized admin access — event_type: security.unauthorized_admin_access
[x] IDOR attempt blocked      — event_type: security.idor_attempt  (hardened only)
```

### Log Format Checklist

```
[x] Machine-parseable format (JSON per line / NDJSON)
[x] UTC timestamps in ISO 8601 format
[x] Consistent field names across all event types
[x] User identifier included on all authenticated events
[x] IP address included on all network events
[x] No passwords, tokens, or sensitive data in log fields
[x] Log level set appropriately (CRITICAL for security events)
[x] Logs written to file and stdout (Docker-friendly)
```

### Log Integrity / Storage Checklist

```
[ ] Log files not writable by the application user in production
    (hardened version runs as non-root; logs directory should be mounted read-write
     by log collector but not by the app process itself in a real deployment)
[ ] Log rotation configured (logrotate or Docker log driver limits)
[ ] Logs forwarded to SIEM (Wazuh, Splunk, ELK) in real deployment
[ ] Log retention policy defined (regulatory minimum often 1 year for financial)
[ ] Alert rules defined in Wazuh for CRITICAL events
```

### Sample Wazuh Rule (ossec.conf)

```xml
<!-- Alert on any CRITICAL log from fintechdemo -->
<rule id="100001" level="12">
  <field name="level">CRITICAL</field>
  <field name="app">fintechdemo</field>
  <description>FintechDemo critical security event: $(message)</description>
</rule>

<!-- Alert on IDOR / suspicious account views -->
<rule id="100002" level="8">
  <field name="event_type">account.viewed</field>
  <field name="detail.suspicious">true</field>
  <description>FintechDemo: Suspicious account access (possible IDOR)</description>
</rule>

<!-- Alert on 3+ failed logins from same IP in 60 seconds -->
<rule id="100003" level="10" frequency="3" timeframe="60">
  <field name="event_type">auth.login_failure</field>
  <description>FintechDemo: Possible brute force - 3+ failures</description>
</rule>
```

---

## 5. Container Security

```
[x] Non-root user in hardened Dockerfile (USER appuser)
[ ] Read-only root filesystem (add: --read-only in docker run)
[ ] No privileged containers
[ ] Minimal base image (python:3.11-slim vs full python:3.11)
[ ] No SSH server in container
[ ] Health check defined in Dockerfile
[ ] Network isolation: internal: true on Docker Compose network for lab use
[ ] Image signed and verified (Docker Content Trust)
[ ] Regular base image updates (weekly rebuild in CI)
```

Sample hardened docker run flags:

```bash
docker run \
  --read-only \
  --tmpfs /tmp \
  --cap-drop ALL \
  --security-opt no-new-privileges \
  --user 1001:1001 \
  fintech-hardened:latest
```

---

## 6. Authentication & Session Hardening

```
[x] bcrypt with work factor >= 10 (hardened: 12)
[x] Unique salt per password (bcrypt handles this automatically)
[ ] Account lockout after N failed attempts (partially done: in-memory rate limit)
[ ] Multi-factor authentication for admin accounts
[x] Session secret key loaded from environment (not hardcoded)
[x] Session secret >= 256 bits / 32 bytes
[x] HttpOnly flag on session cookie
[x] SameSite=Strict on session cookie (CSRF mitigation)
[ ] Secure flag on session cookie (requires HTTPS — acceptable in lab)
[x] Session timeout after 30 minutes of inactivity (hardened)
[ ] CSRF token on all state-changing forms (not implemented — use Flask-WTF)
[ ] Session invalidated on logout (session.clear() used — correct)
```

---

## 7. CI/CD Pipeline Integration

Suggested pipeline stage order (shift-left security):

```
1. Pre-commit hooks      → detect-secrets, trailing-whitespace
2. PR/commit validation  → bandit, semgrep (fail build on HIGH findings)
3. Build                 → Docker image build
4. SCA scan              → trivy image, pip-audit (fail on CRITICAL CVEs)
5. Integration test      → functional tests
6. Deploy (staging)      → runtime WAF rules enabled
7. Deploy (production)   → same, plus SIEM alerts active
```

Sample GitHub Actions snippet:

```yaml
- name: Run Bandit SAST
  run: |
    pip install bandit
    bandit -r vulnerable/ -ll -ii   # fail on medium severity
    
- name: Run pip-audit
  run: |
    pip install pip-audit
    pip-audit -r vulnerable/requirements.txt

- name: Scan for secrets
  uses: trufflesecurity/trufflehog@main
  with:
    path: ./
    base: ${{ github.event.repository.default_branch }}
```

---

## Overall Risk Rating

| Control Area          | Vulnerable | Hardened |
|-----------------------|------------|----------|
| Injection prevention  | ❌ FAIL     | ✅ PASS  |
| Password storage      | ❌ FAIL     | ✅ PASS  |
| Object-level AuthZ    | ❌ FAIL     | ✅ PASS  |
| Transfer authorization| ❌ FAIL     | ✅ PASS  |
| Secret management     | ❌ FAIL     | ✅ PASS  |
| Security headers      | ⚠️  PARTIAL | ✅ PASS  |
| Logging coverage      | ✅ PASS     | ✅ PASS  |
| CSRF protection       | ❌ FAIL     | ❌ FAIL  |
| Container hardening   | ❌ FAIL     | ⚠️  PARTIAL |
| Rate limiting         | ⚠️  PARTIAL | ⚠️  PARTIAL |

**Note:** CSRF and full container hardening are marked as remaining work items
in the hardened version to keep the demo stack simple. They are documented in
the checklist above and would be addressed before any real deployment.
