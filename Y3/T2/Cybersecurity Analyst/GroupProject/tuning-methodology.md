# 🔧 Rule Tuning Methodology

## Overview

This document describes the tuning decisions made during the deployment of Wazuh and Suricata rules in the lab. The goal was to maximize detection fidelity while minimizing false positives in a controlled lab environment.

---

## Tuning Principles

1. **Start broad, narrow down** — Begin with default/community rules, observe noise, then suppress or threshold high-FP rules
2. **Alert on behavior, not tools** — Tune rules to detect the *technique* (e.g., rapid auth failures), not just specific tool signatures
3. **Document every decision** — Each suppression or threshold change is logged with the reason
4. **Map to ATT&CK** — Every retained rule has an ATT&CK mapping to ensure coverage gaps are visible
5. **Preserve forensic value** — Low-severity informational rules are retained at lower levels rather than disabled

---

## Wazuh Rule Tuning

### SSH Brute Force (Rules 5710, 5712, 5763)

**Problem:** Default threshold was 3 failures — generates excessive alerts on shared dev servers where multiple users occasionally mistype credentials.

**Change:** Raised frequency to `8 failures in 60 seconds`.

```xml
<!-- Original Wazuh built-in behavior: 3 failures -->
<!-- Tuned custom rule -->
<rule id="100001" level="10" frequency="8" timeframe="60">
  <if_matched_sid>5710</if_matched_sid>
  <description>SSH brute force: 8+ failures in 60 seconds</description>
  <mitre><id>T1110.001</id></mitre>
</rule>
```

**Rationale:** 8 failures in 60 seconds eliminates accidental lockout noise while still catching automated tools like Hydra (which produce 50+ failures/second).

---

### RDP Brute Force (Custom Rule 100100)

**Problem:** No built-in Wazuh rule triggered reliably for RDP brute force in our lab topology. Windows Event ID 4625 fired but wasn't escalated to actionable severity.

**Change:** Created custom rule correlating Windows Event ID 4625 with RDP-specific fields.

```xml
<rule id="100100" level="10">
  <if_group>windows</if_group>
  <field name="win.system.eventID">^4625$</field>
  <field name="win.eventdata.logonType">^3$</field>
  <description>RDP Attack Detected: repeated network logon failures</description>
  <mitre><id>T1110.003</id></mitre>
  <group>rdp,authentication_failures,brute_force</group>
</rule>
```

---

### Encoded PowerShell (Rules 61603, 92057, 92200, 92213)

**Problem:** Built-in rules 61603 and 92200 didn't fire — Sysmon integration required additional configuration.

**Observation:** Rules `92057` and `92213` fired instead, which are higher-fidelity detections specifically for base64-encoded commands. This is a *better* outcome than the expected rules.

**Decision:** No tuning needed; documented the behavioral difference. Custom rule added to cross-correlate with process creation events.

---

### File Integrity Monitoring (FIM)

**Critical paths monitored:**

```xml
<!-- Linux critical paths -->
<directories check_all="yes" report_changes="yes" realtime="yes">
  /etc,/bin,/sbin,/usr/bin,/var/www
</directories>

<!-- Windows critical paths -->
<directories check_all="yes" report_changes="yes" realtime="yes">
  C:\inetpub\wwwroot,C:\Windows\System32
</directories>
```

**Tuning:** Added `report_changes="yes"` to capture the diff of file modifications, not just that a change occurred. Critical for forensic analysis.

---

## Suricata Rule Tuning

### Network Configuration for Lab

**Problem:** Default Suricata config uses `EXTERNAL_NET: !$HOME_NET` which excludes intra-LAN traffic. All our attacks originate within the lab network.

**Change:**
```yaml
# suricata.yaml
vars:
  address-groups:
    HOME_NET: "[192.168.1.0/24]"
    EXTERNAL_NET: "any"   # ← Changed from !$HOME_NET for lab testing
```

> ⚠️ **Production Note:** Revert `EXTERNAL_NET` to `!$HOME_NET` in production deployments to avoid performance impact from inspecting all internal traffic.

---

### Emerging Threats Ruleset Categories

Rules were selectively enabled by category based on the attack scenarios planned:

| Category | Enabled | Rationale |
|---|:---:|---|
| `ET SCAN` | ✅ | Port scanning, SSH/RDP brute force detection |
| `ET WEB_SERVER` | ✅ | SQL injection, web shell, XSS detection |
| `ET POLICY` | ✅ | C2 beaconing, IRC, suspicious protocols |
| `ET DOS` | ✅ | ICMP flood, volumetric attacks |
| `ET MALWARE` | ✅ | Known malware C2 signatures |
| `ET EXPLOIT` | ✅ | Exploitation attempt signatures |
| `ET P2P` | ❌ | Not relevant to lab scenarios |
| `ET USER_AGENTS` | ❌ | Too noisy for lab environment |

---

### Threshold Tuning for ICMP

**Problem:** ICMP flood generated ~3.15M packets in 5 minutes, which caused alert volume spikes.

**Change:** Added threshold to prevent alert storm:

```
threshold gen_id 1, sig_id 2100366, type threshold, track by_src, count 1000, seconds 10
```

This ensures one alert per 1000 ICMP packets per 10-second window rather than alerting on every packet.

---

## False Positive Management

| Rule | FP Type Observed | Resolution |
|---|---|---|
| Wazuh `5710` | Dev SSH key mismatches | Raised threshold to 8 failures |
| Suricata `2010935` | Legitimate MSSQL health checks | Added source IP whitelist for monitoring tools |
| Wazuh `554` | IIS log rotation creating files | Added exclusion for IIS log directories |
| Suricata ICMP rules | Network monitoring pings | Whitelisted monitoring server IP |

---

## Lessons Learned

1. **FIM is extremely sensitive** — It will catch nearly everything that touches the filesystem; tune exclusions carefully for web log directories
2. **Suricata `EXTERNAL_NET: any` doubles inspection load** — Only use for lab/testing; profile CPU usage before deploying to production
3. **Base64 PowerShell detection is reliable** — Rules `92057`/`92213` are well-maintained and low-FP in our environment
4. **Multi-SID SQL injection detection adds value** — SQLmap triggered 4 SIDs simultaneously, giving more forensic context than a single rule would
