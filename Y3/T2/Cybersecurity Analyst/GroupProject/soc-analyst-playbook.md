# 📋 SOC Analyst Playbook

Quick reference response procedures for every alert type detected in this lab. Designed for Tier 1/2 analysts using the Wazuh Dashboard.

---

## Alert Triage Workflow

```
Alert Fires
    │
    ▼
1. ASSESS severity level
   ├── Level 13-15 → Immediate escalation (P1)
   ├── Level 10-12 → Investigate within 15 min (P2)
   ├── Level 7-9  → Review within 1 hour (P3)
   └── Level 0-6  → Queue for daily review (P4)
    │
    ▼
2. IDENTIFY source IP, destination, time window
    │
    ▼
3. CORRELATE with other alerts (same source IP in 30-min window)
    │
    ▼
4. DETERMINE if false positive (check whitelist)
    │
    ├── FALSE POSITIVE → Document, suppress if recurring, close
    └── TRUE POSITIVE  → Execute playbook below
                │
                ▼
            5. CONTAIN → INVESTIGATE → REMEDIATE → DOCUMENT
```

---

## Playbook: Authentication Attacks

**Triggered by:** Rules `5763`, `100100`, `5710`, `5712`

### SSH Brute Force Response
```
Immediate (0-5 min):
  □ Capture source IP from alert
  □ Run: grep "Accepted" /var/log/auth.log | grep <source_ip>
  □ If successful login found → ESCALATE to P1
  □ Block source IP: ufw deny from <source_ip>

Short-term (5-30 min):
  □ Review all auth attempts: grep <source_ip> /var/log/auth.log
  □ Check for any new local accounts: getent passwd | sort -t: -k3 -n | tail -20
  □ Verify SSH config: grep PermitRootLogin /etc/ssh/sshd_config
  □ Enable Fail2Ban if not active: systemctl status fail2ban

Documentation:
  □ Record source IP, timestamp, attempt count, outcome
  □ Tag alert with case ID in Wazuh Dashboard
```

### RDP Brute Force Response
```
Immediate (0-5 min):
  □ Check Windows Security Log: Event ID 4624 from source IP
  □ If successful logon found → ESCALATE to P1
  □ Block at firewall: netsh advfirewall rule block <source_ip>

Short-term (5-30 min):
  □ Review Event IDs 4625, 4624, 4634 for full session history
  □ Check for new accounts: net user | sort
  □ Review RDP session list: qwinsta
  □ Verify Account Lockout Policy is active
```

---

## Playbook: Network Reconnaissance

**Triggered by:** Suricata `86601`, `sid:2010935`, `sid:2009582`

```
Assessment (0-10 min):
  □ Identify all ports scanned (review Suricata flow log)
  □ Determine if source IP is internal or external
  □ Check if source IP is in authorized scanner whitelist
  □ Review scan timing (business hours vs. off-hours)

Response:
  □ Internal unauthorized scan:
      → Notify asset owner of source IP
      → Block at internal firewall segment
      → Investigate for compromise of source host
  □ External scan:
      → Block at perimeter immediately
      → Log for threat intelligence
      → Escalate if targeting critical services

Post-incident:
  □ Cross-reference with auth logs: did scan precede login attempt?
  □ Document all targeted services
```

---

## Playbook: File Integrity Alerts

**Triggered by:** Rules `550`, `554`, `2832`

### Critical File Modified (Rule 550)
```
Immediate:
  □ Identify modified file from alert details
  □ View the change diff in Wazuh Dashboard (FIM tab)
  □ Verify change was authorized (change management system)
  □ If unauthorized: ISOLATE host from network

Forensic Steps:
  □ Capture: who, what, when (check auth.log timestamps)
  □ Hash the current file: sha256sum <file>
  □ Retrieve clean version from backup
  □ Check for additional modified files in same directory
```

### Web Shell Detected (Rule 554)
```
CRITICAL — Assume full host compromise

Immediate:
  □ ISOLATE web server from network
  □ Preserve: memory dump, running processes, open connections
  □ DO NOT delete web shell before forensic review

Investigation:
  □ Web access logs: grep <shell_filename> /var/log/apache2/access.log
  □ Identify upload vector (CVE, misconfigured upload, stolen credentials)
  □ Search for additional shells: find /var/www -name "*.php" -newer <date>

Remediation:
  □ Remove web shell(s)
  □ Patch upload vulnerability
  □ Rotate all web application credentials
  □ Rebuild from clean image if compromise scope unclear
```

---

## Playbook: Malware & Process Execution

**Triggered by:** Rules `62138`, `87105`, `92057`, `92213`

### Encoded PowerShell (Level 12)
```
Immediate:
  □ Decode base64 payload:
    [System.Text.Encoding]::Unicode.GetString([Convert]::FromBase64String("<payload>"))
  □ Assess decoded command intent
  □ Identify executing user account

Investigation:
  □ PowerShell script block logging: Get-WinEvent -LogName "Microsoft-Windows-PowerShell/Operational"
  □ Parent process: check Event ID 4688
  □ Network connections made during execution: Event ID 5156

Response:
  □ Level 12+: Treat as confirmed threat
  □ Isolate endpoint pending investigation
  □ Rotate credentials for executing account
```

---

## Playbook: C2 & Network Attacks

**Triggered by:** Suricata `86601` (ET CHAT, ET POLICY)

### C2 Beaconing / IRC
```
Immediate:
  □ Block outbound port 6667 at perimeter firewall
  □ Identify process on victim: netstat -tunap | grep 6667
  □ Kill malicious connection: kill -9 <pid>
  □ ISOLATE affected host

Investigation:
  □ Capture network traffic: tcpdump -i any -w c2_capture.pcap port 6667
  □ Analyze IRC commands exchanged
  □ Check for additional C2 channels (other non-standard ports)
  □ Scan for malware/backdoors: clamscan -r /
```

### SQL Injection (Critical — Suricata P1)
```
Immediate:
  □ Block attacker IP at WAF/firewall
  □ Check web application logs for successful extraction:
    grep "UNION SELECT" /var/log/apache2/access.log
  □ Review database slow query log for anomalous queries

Assessment:
  □ Did attacker reach the database?
  □ Was any data exfiltrated (review DB query log)?
  □ Are database credentials compromised?

Remediation:
  □ Patch vulnerable SQL input with parameterized queries
  □ Rotate all DB credentials
  □ Enable WAF rules for SQL injection patterns
  □ Audit all web app input fields
```

---

## Dashboard Queries

Quick Lucene queries for the Wazuh Dashboard:

```
# All high severity alerts (last 24h)
rule.level:>=10 AND @timestamp:[now-24h TO now]

# Specific source IP investigation
data.srcip:"192.168.1.131"

# Authentication failures
rule.groups:"authentication_failures"

# File integrity alerts
rule.groups:"syscheck"

# All Suricata alerts
data.alert.action:"allowed" AND agent.name:"suricata"

# Brute force attempts
rule.id:(5710 OR 5712 OR 5763 OR 100100)
```

---

*Playbook version: 1.0 · March 2026 · CADT SOC Lab*
