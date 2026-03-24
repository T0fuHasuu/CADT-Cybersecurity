# Demo Script — FintechDemo Banking Security Lab
## 15-Minute Classroom Presentation

**Audience:** Developers, security students, pentesters-in-training
**Format:** Live demo with both browser windows open side-by-side
**Setup time:** 5 minutes before class (see Pre-Demo Checklist below)

---

## Pre-Demo Checklist

Run through this 5 minutes before presenting:

```
[ ] docker compose up --build  (started, both containers healthy)
[ ] Browser tab 1: http://localhost:5000  (Vulnerable — label it "VULNERABLE")
[ ] Browser tab 2: http://localhost:5001  (Hardened  — label it "HARDENED")
[ ] Browser tab 3: Terminal window visible (for curl commands)
[ ] Test login: alice / password123 on BOTH apps — confirm they work
[ ] Log out of both apps
[ ] Open a second incognito window for the "attacker" view
[ ] Reset databases if a previous demo ran: docker compose down -v && docker compose up
[ ] Zoom browser to 125% so the audience can read the screen
```

**Screen layout suggestion:**
```
Left half:  Browser — Vulnerable app (http://5000)
Right half: Browser — Hardened app (http://5001)
Bottom:     Terminal (minimized, ready to bring up for curl demos)
```

---

## Introduction [0:00 – 1:30]

**Say:**
> "Today we're going to do something different. Instead of just reading about
> security vulnerabilities, we're going to *exploit* them in real time — against
> a fake banking app I've built specifically for this purpose. Everything you're
> about to see is intentional. The app is running locally, completely offline,
> with entirely fictional users and balances.
>
> We're going to tell one continuous story: one attacker, one goal — steal money
> from the admin's bank account. We'll follow three steps, each one enabled by a
> different coding mistake. Then we'll flip to the fixed version and see exactly
> what stops each attack.
>
> The three vulnerabilities are: SQL injection, insecure direct object reference,
> and missing server-side authorization. If those sound abstract right now, they
> won't in 12 minutes."

**Show:** Both browser tabs side by side. Point out the banner colours.
- Purple banner = vulnerable version
- Green banner  = hardened version

---

## Scene Setting [1:30 – 2:30]

**Say:**
> "Meet Alice. She's a regular user at FintechDemo Bank. She has one checking
> account with about $2,450 in it. She knows there are other users in the system.
> Her goal: find a high-value account and transfer money from it into hers —
> without knowing the other user's password.
>
> Let me show you the login page first — it looks completely ordinary."

**Do:**
- Show the vulnerable login page at http://localhost:5000
- Point out the username and password fields
- Note the credentials panel at the bottom (demo convenience only)

---

## Attack Step 1 — SQL Injection [2:30 – 6:00]

### Explain first (1 minute)

**Say:**
> "The first vulnerability is in how the login form is processed by the server.
> Instead of using a safe API call, the developer built the SQL query using
> Python string formatting — essentially gluing the username directly into the
> database command.
>
> Watch what that means. When alice types her username, the server builds this:"

**Show on projector / whiteboard or paste in a text editor:**
```sql
SELECT * FROM users WHERE username='alice' AND password='...'
```

**Say:**
> "Normal. But what if we put a single quote and two dashes into the username?
> The database sees this:"

```sql
SELECT * FROM users WHERE username='admin'--' AND password='...'
```

**Say:**
> "The `--` is a SQL comment. Everything after it is ignored. The password check
> disappears entirely. We're now asking the database: just give me the admin user.
> And it does."

### Live exploit (1 minute)

**Do (on vulnerable app, http://localhost:5000):**
1. In the username field, type exactly: `admin'--`
2. In the password field, type anything: `wrongpassword`
3. Click Sign In

**Point out:**
- Admin banner appears in the navigation
- Admin menu item visible
- Welcome message says "System Admin"

**Say:**
> "We are now logged in as the administrator. No password needed. We didn't
> crack anything, we didn't brute force anything — we exploited a coding
> mistake to change what question the database was asked."

### Fix (30 seconds)

**Switch to hardened app (http://localhost:5001):**
1. Type `admin'--` in username, `wrongpassword` in password
2. Click Sign In

**Show:** Login fails. "Invalid username or password."

**Say:**
> "In the hardened version, the SQL query uses a parameterized placeholder — a
> question mark. The database driver treats the entire username as a data value,
> not as SQL syntax. The quote character is just a quote character now. The
> comment never executes. The fix is literally one line of code change."

**Show code snippet** (optional — have vulnerable/auth.py and hardened/auth.py open in editor):
```python
# Vulnerable
f"WHERE username='{username}'"

# Hardened
"WHERE username=?", (username,)
```

---

## Attack Step 2 — IDOR [6:00 – 9:00]

### Return to vulnerable app, logged in as alice (switch back or use incognito)

**Say:**
> "Now let's use Alice's real credentials — no tricks needed here. Once she's
> authenticated, she can navigate to any account in the system just by changing
> the URL."

**Do (on vulnerable app):**
1. Log in as alice / password123
2. Show the dashboard — account ACC001, balance $2,450.75
3. In the URL bar, change `/dashboard` to `/account/ACC004`
4. Press Enter

**Point out:**
> "We're looking at the administrator's account. $50,000. And we can see every
> transaction. The server never asked: does alice own this account? It just
> fetched it and showed it to whoever asked.
>
> Account IDs here are short strings: ACC001 through ACC006. Enumerate all of
> them in under a second."

**Optionally show the hint panel on the dashboard or manually visit each one.**

**Say:**
> "This is called an Insecure Direct Object Reference — IDOR. The identifier in
> the URL directly references a database record, and there's no check that the
> current user is allowed to see that record.
>
> We now know the target: account ACC004, $50,000. Time for the final step."

### Fix (30 seconds)

**Switch to hardened app (http://localhost:5001):**
1. Log in as alice
2. Try to navigate to `/account/ACC004`

**Show:** 403 Forbidden error page

**Say:**
> "Two lines of code. After fetching the account from the database, the hardened
> version checks whether the account's owner ID matches the session user ID. If
> not — 403. And this is logged as a security event for the SIEM to pick up."

---

## Attack Step 3 — Unauthorized Transfer [9:00 – 12:30]

### Setup the climax

**Say:**
> "Here's where it gets interesting. Alice is logged in with her own credentials.
> She goes to the transfer page. The form shows only her own accounts in the
> dropdown. Everything looks safe.
>
> But the server has a fatal assumption: it trusts that whatever `from_account`
> value arrives in the POST body was put there by the legitimate dropdown. It
> never checks."

### Browser exploit (1 minute)

**Do (on vulnerable app, logged in as alice):**
1. Go to http://localhost:5000/transfer
2. Show the transfer form — only ACC001 in the "From" dropdown
3. Open browser DevTools (F12) → Inspector/Elements
4. Find the `<select name="from_account">` element
5. Edit the `value` of the first option from `ACC001` to `ACC004`
6. Set amount to `10000`, destination to `ACC001`, description to `test`
7. Click Send Transfer

**Point out:**
- Success message: "$10,000.00 from ACC004 to ACC001 completed"
- Go to dashboard — Alice's balance is now $12,450.75

**Say:**
> "The server processed a $10,000 transfer FROM the admin's account — initiated
> using Alice's session. The dropdown was a courtesy to the user, not a security
> control. Any data in an HTTP request body can be modified before sending."

### Curl version (optional, 30 seconds — shows this works without a browser)

```bash
curl -s -X POST http://localhost:5000/transfer \
  -H "Content-Type: application/x-www-form-urlencoded" \
  -b "session=<ALICE_COOKIE>" \
  -d "from_account=ACC004&to_account=ACC001&amount=1000"
```

**Say:**
> "You don't even need a browser. A single curl command. The form's UI is
> completely irrelevant to the attack."

### Fix (30 seconds)

**Switch to hardened app (http://localhost:5001):**
1. Log in as alice
2. Try the same DevTools manipulation on the transfer form
3. Submit

**Show:** 403 Forbidden

**Say:**
> "One if statement. After fetching the source account, the server asks: does
> `account.user_id` equal `session['user_id']`? If not, the request is rejected
> before touching any balances. The ownership record comes from the database —
> the most trusted source in the system. No client input can change it."

---

## The Full Chain — What Just Happened [12:30 – 13:30]

**Say:**
> "Let's summarize what we did:"

**Draw or show on whiteboard:**

```
VULN 1: SQL Injection
  → Bypassed authentication entirely
  → Or: used our own credentials (VULN 1 optional for VULN 2 and 3)
       
VULN 2: IDOR
  → Enumerated all accounts, discovered $50,000 target (ACC004)
  
VULN 3: Missing Authorization
  → Transferred $10,000 from admin's account to ours
  → Server never checked ownership
  
IMPACT: $10,000 stolen, no password cracked, no malware deployed
```

**Say:**
> "Three different files. Three different developers might have written each one.
> Each mistake seems minor in isolation. Together they form a complete exploit chain.
>
> Now — what's the common thread?"

**Pause for audience answers. Lead toward:**

> "Every single one is about the server trusting input it received from the client.
> The login trusted the SQL string. The account route trusted the URL parameter.
> The transfer trusted the form field. Security means never trusting what the
> client sends — verifying it server-side, against authoritative data."

---

## Wrap-Up & Secure Coding Principles [13:30 – 15:00]

**Say:**
> "Three rules to take from today:"

```
1. NEVER concatenate user input into a query string.
   Always use parameterized queries. Every time.

2. ALWAYS check object ownership server-side.
   Authentication (are you logged in?) ≠ Authorization (can you touch THIS?).

3. NEVER trust client-supplied identifiers for operations that modify data.
   The dropdown, the hidden field, the URL parameter — all can be changed.
   The database is your source of truth.
```

**Say:**
> "These aren't exotic findings. They're in the OWASP Top 10 every single year.
> They're found in production banking, healthcare, and government systems regularly.
> The fix in each case was one or two lines of code. The cost of not fixing them?
> In a real system, significant financial and reputational damage."

**Final note:**

> "The logs you saw being generated throughout this demo — those JSON event
> streams — are exactly what a SIEM like Wazuh ingests. In a real deployment,
> the three attacks we just performed would have each generated a CRITICAL alert.
> Let me show you what those logs look like."

**Show terminal:** `tail -f logs/vulnerable.log` and scroll through the events.

> "Security isn't just about preventing attacks. It's about detecting them,
> responding to them, and having the evidence you need when something goes wrong."

---

## Q&A Prompts

If time allows, these questions often drive good discussion:

- "Could you combine VULN 2 and VULN 3 without exploiting VULN 1 first? What would change?"
- "What if account IDs were UUIDs instead of ACC001/ACC002? Would VULN 2 still work?"
  *(Answer: IDOR by UUID is still IDOR — security by obscurity is not security.)*
- "The hardened transfer fix uses a single if statement. What else would you add?"
  *(CSRF token, 2FA for large transfers, velocity limits)*
- "Why is bcrypt better than SHA-256 for passwords if SHA-256 is 'more secure'?"
  *(bcrypt is slow by design — key stretching — SHA-256 is fast, wrong property)*
