# ============================================================
#  SSRF DEMO — PRESENTER SCRIPT
#  Section 5: Demo
# ============================================================

## SETUP (do this before presenting)

  bash setup.sh    # once, to install deps
  bash run.sh      # start both services

  Open browser → http://<VM-IP>:5000


## ── ACT 1: NORMAL BEHAVIOR ──────────────────────────────────

  NARRATION:
    "This is LinkPeek — a URL preview service.
    You give it a URL, it fetches the content server-side
    and displays it. Perfectly normal feature."

  DEMO:
    Paste:  https://httpbin.org/get
    → Click Fetch
    → Show the JSON response from an external site
    → "The server fetched this for us. Standard behavior."


## ── ACT 2: SHOW THE TRUST BOUNDARY ──────────────────────────

  NARRATION:
    "Now — there's an internal service running on port 8080.
    It's only bound to localhost. Let me try to reach it directly
    from MY machine (the attacker's host)."

  DEMO:
    From attacker machine, open a new tab and go to:
    http://<VM-IP>:8080/api/config
    → Connection refused / timeout
    → "I cannot reach it. The port is not exposed."

    "But the server itself can reach it. That's the trust boundary."


## ── ACT 3: FIRST ATTEMPT — BLOCKED ──────────────────────────

  NARRATION:
    "A developer put a basic protection in place.
    Let me try the obvious path and see what happens."

  DEMO:
    Paste:  http://localhost:8080/api/config
    → Click Fetch
    → Show BLOCKED response: "contains disallowed keyword 'localhost'"

    Paste:  http://127.0.0.1:8080/api/config
    → Click Fetch
    → Show BLOCKED response: "contains disallowed keyword '127.0.0.1'"

    "Good — it's checking for localhost and 127.0.0.1.
    The developer tried to prevent this. But is it enough?"


## ── ACT 4: THE BYPASS ────────────────────────────────────────

  NARRATION:
    "The blocklist only checks for specific strings.
    There are other ways to refer to the loopback interface.
    On Linux, 0.0.0.0 also routes to localhost."

  DEMO:
    Paste:  http://0.0.0.0:8080/api/config
    → Click Fetch
    → Response comes back with ALL the secrets

    Point out in the response (highlighted in red):
      ✗  db_pass:              Tr0ub4dor&3_secure!
      ✗  internal_api_key:     sk-internal-9f3a2b1c8e7d6f4a5b0c
      ✗  admin_jwt_secret:     HS256-secret-do-not-share-99xZq
      ✗  aws_access_key_id:    AKIAIOSFODNN7EXAMPLE
      ✗  aws_secret_access_key: wJalrXUtnFEMI/...

    "The server fetched it. Not me — the SERVER.
     I never spoke to port 8080 directly.
     The server did it on my behalf."


## ── OTHER BYPASS OPTIONS (mention verbally, do not demo) ─────

  http://127.1:8080/api/config         # shorthand IP notation
  http://[::1]:8080/api/config         # IPv6 loopback
  http://0177.0.0.1:8080/api/config    # octal notation
  http://2130706433:8080/api/config    # decimal IP


## ── KEY POINTS TO EMPHASIZE ──────────────────────────────────

  1. The attacker never directly contacted the internal service
  2. The blocklist approach failed — allowlist is the right fix
  3. The server's own network access became the weapon
  4. One feature → full credential exposure


## ── WHAT NOT TO DO DURING DEMO ──────────────────────────────

  - Do not show multiple bypass techniques (confuses audience)
  - Do not show the source code during the demo (keep focus)
  - Do not rush through Act 2 — the trust boundary is the key idea


## ── TRANSITION TO SLIDE 6 ────────────────────────────────────

  "So how do we prevent this?
   The developer used a blocklist. The right answer is an allowlist.
   Only permit the URLs your app actually needs.
   Next slide covers prevention."
