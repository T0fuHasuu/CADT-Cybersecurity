# 🗺️ MITRE ATT&CK Mapping

This document maps every detection rule and test case in the lab to the [MITRE ATT&CK Enterprise Matrix](https://attack.mitre.org/). Mappings follow the v14 framework.

---

## Full Coverage Matrix

| Test # | Attack Scenario | Tactic | Technique | Sub-Technique | ATT&CK ID | Detection Source |
|:---:|---|---|---|---|---|---|
| 1 | SSH Brute Force | Credential Access | Brute Force | Password Guessing | **T1110.001** | Wazuh `5763` |
| 2 | RDP Brute Force | Credential Access | Brute Force | Password Spraying | **T1110.003** | Wazuh `100100` |
| 3 | TCP SYN Port Scan | Discovery | Network Service Discovery | — | **T1046** | Suricata `sid:2010935` |
| 4 | Service Version Enumeration | Discovery | Network Service Discovery | — | **T1046** | Suricata `sid:2010935` |
| 5 | `/etc/passwd` Modification | Persistence | Create Account | Local Account | **T1136.001** | Wazuh `550` (FIM) |
| 6 | Web Shell Upload | Persistence | Server Software Component | Web Shell | **T1505.003** | Wazuh `554` (FIM) |
| 7 | Malicious Cron Job | Persistence | Scheduled Task/Job | Cron | **T1053.003** | Wazuh `2832` |
| 8 | EICAR Test File | Execution | User Execution | Malicious File | **T1204.002** | Wazuh `62138` |
| 9 | Encoded PowerShell | Defense Evasion | Obfuscated Files or Info | Command Obfuscation | **T1027.010** | Wazuh `92057`, `92213` |
| 10 | Netcat C2 (IRC) | Command & Control | Application Layer Protocol | Standard Encoding | **T1071** | Suricata `86601` |
| 11 | SQL Injection via DVWA | Initial Access | Exploit Public-Facing Application | — | **T1190** | Suricata `sid:2006446` |
| 12 | ICMP Flood & Ping Sweep | Impact | Network Denial of Service | Direct Network Flood | **T1498.001** | Suricata `sid:2100366` |

---

## Tactic Coverage

```
Initial Access       ██░░░░░░░░  1 technique  (T1190)
Execution            ██░░░░░░░░  1 technique  (T1204.002)
Persistence          ██████░░░░  3 techniques (T1136.001, T1505.003, T1053.003)
Defense Evasion      ██░░░░░░░░  1 technique  (T1027.010)
Credential Access    ████░░░░░░  2 techniques (T1110.001, T1110.003)
Discovery            ██░░░░░░░░  1 technique  (T1046)
Command & Control    ██░░░░░░░░  1 technique  (T1071)
Impact               ██░░░░░░░░  1 technique  (T1498.001)
```

---

## Rule-to-ATT&CK Cross Reference

### Wazuh Rules

| Rule ID | Description | ATT&CK ID |
|---|---|---|
| `5763` | sshd: brute force trying to get access | T1110.001 |
| `100100` | RDP Attack Detected (custom) | T1110.003 |
| `550` | Integrity checksum changed | T1136.001, T1505.003 |
| `554` | File added to the system | T1505.003 |
| `2832` | Crontab entry changed | T1053.003 |
| `62138` | FIM: EICAR-like file created | T1204.002 |
| `92057` | PowerShell Base64-encoded command | T1027.010 |
| `92213` | Suspicious PowerShell flags detected | T1027.010 |

### Suricata Rules

| SID | ET Category | Description | ATT&CK ID |
|---|---|---|---|
| `2010935` | ET SCAN | Suspicious inbound to MSSQL 1433 | T1046 |
| `2009582` | ET SCAN | Host Discovery / Ping Sweep | T1046 |
| `2006446` | ET WEB_SERVER | SQL Injection UNION SELECT | T1190 |
| `2053467` | ET WEB_SERVER | SQL Injection SELECT CAST | T1190 |
| `2016935` | ET WEB_SERVER | SQL Injection SELECT SLEEP | T1190 |
| `2100366` | GPL ICMP | ICMP PING \*NIX | T1498.001 |
| `2100469` | ET SCAN | ICMP Flood detection | T1498.001 |
| `86601` | ET CHAT | IRC C2 communication channel | T1071 |
| `sid:100100` | Custom RDP | RDP brute force attempt | T1110.003 |

---

## Navigator Layer

A pre-built [ATT&CK Navigator](https://mitre-attack.github.io/attack-navigator/) layer JSON is available at:

```
docs/attck-navigator-layer.json
```

Import this file into the ATT&CK Navigator to visualize coverage.

---

## Notes on Mapping Methodology

1. **Technique selection** was based on the adversary's *intent* and *method*, not just the tool used.
2. Where behavior matched multiple techniques (e.g., EICAR could be T1204 or T1566), the **most specific confirmed technique** was selected.
3. Sub-technique IDs are preferred when the activity clearly matches sub-technique criteria.
4. All mappings reference **ATT&CK Enterprise Matrix v14**.
