<<<<<<< HEAD
<div align="center">

<img src="https://img.shields.io/badge/Wazuh-5.0+-00A9CE?style=for-the-badge&logo=wazuh&logoColor=white"/>
<img src="https://img.shields.io/badge/Suricata-8.0.3-EF3B2D?style=for-the-badge&logoColor=white"/>
<img src="https://img.shields.io/badge/MITRE%20ATT%26CK-Mapped-red?style=for-the-badge"/>
<img src="https://img.shields.io/badge/Rules-15%2B%20Custom-brightgreen?style=for-the-badge"/>
<img src="https://img.shields.io/badge/Tests-12%2F12%20Passed-success?style=for-the-badge"/>
<img src="https://img.shields.io/badge/License-MIT-blue?style=for-the-badge"/>

<br/><br/>

# 🛡️ Open-Source SOC: Wazuh + Suricata Detection Lab

### *A Production-Grade SIEM & IDS Integration with 15+ Custom Detection Rules, 12 Validated Attack Scenarios, and Full MITRE ATT&CK Mapping*

<br/>

> **Bachelor of Cybersecurity — Cambodia Academy of Digital Technology**  
> Department of Telecommunications and Networking · March 2026

<br/>

[📋 View Attack Report](#-attack-validation-report) · [🗺️ MITRE Mapping](#️-mitre-attck-mapping) · [⚙️ Rules](#️-detection-rules) · [🚀 Quick Start](#-quick-start) · [📁 Docs](./docs/)

</div>

---

## 📌 Table of Contents

- [Project Overview](#-project-overview)
- [Architecture](#-architecture)
- [Detection Coverage](#-detection-coverage)
- [Attack Validation Report](#-attack-validation-report)
- [MITRE ATT&CK Mapping](#️-mitre-attck-mapping)
- [Detection Rules](#️-detection-rules)
- [Lab Environment](#-lab-environment)
- [Quick Start](#-quick-start)
- [Repository Structure](#-repository-structure)
- [Results & Findings](#-results--findings)
- [Team](#-team)

---

## 🎯 Project Overview

This repository documents the full implementation of an **open-source Security Operations Center (SOC)** built on [Wazuh](https://wazuh.com/) (SIEM/HIDS) and [Suricata](https://suricata.io/) (NIDS). The project was designed, deployed, and validated over a 4-week sprint as a capstone for a Bachelor of Cybersecurity program.

### What's Inside

| Component | Details |
|---|---|
| 🔍 **SIEM Platform** | Wazuh 5.x — log analysis, FIM, vulnerability detection, alert correlation |
| 🌐 **NIDS Engine** | Suricata 8.0.3 — deep packet inspection, 48,919 Emerging Threats rules loaded |
| 📏 **Custom Rules** | 15+ hand-tuned Suricata + Wazuh rules with documented tuning methodology |
| 🧪 **Attack Tests** | 12 controlled attack simulations across 5 threat categories |
| 🗺️ **ATT&CK Mapping** | Every detection mapped to MITRE ATT&CK Tactics, Techniques & Sub-techniques |
| ✅ **Detection Rate** | **12/12 attacks detected** — 100% validation pass rate |

### Why This Matters

> Commercial SIEM solutions can cost **$50,000–$500,000+/year**. This stack costs **$0** and achieves enterprise-grade detection coverage across host, network, and application layers — making it viable for academic institutions, startups, and resource-constrained organizations.

---

## 🏗️ Architecture

```
┌─────────────────────────────────────────────────────────────────────┐
│                        SOC Lab Network                               │
│                                                                      │
│  ┌─────────────┐    ┌──────────────┐    ┌──────────────────────┐   │
│  │   Attacker  │    │ Linux/Windows│    │   Suricata Sensor    │   │
│  │  Kali Linux │    │  Endpoints   │    │  (Network Monitor)   │   │
│  │192.168.1.131│    │Wazuh Agents  │    │  Traffic Mirroring   │   │
│  └──────┬──────┘    └──────┬───────┘    └──────────┬───────────┘   │
│         │                  │                        │               │
│         └──────────────────┴───────────────────┐   │               │
│                         Internal LAN            │   │               │
│                    ┌────────────────────────────▼───▼────────────┐  │
│                    │           Wazuh Server (Manager)            │  │
│                    │  ┌──────────┐ ┌─────────┐ ┌─────────────┐  │  │
│                    │  │  Wazuh   │ │  Wazuh  │ │   Wazuh     │  │  │
│                    │  │ Manager  │ │ Indexer │ │  Dashboard  │  │  │
│                    │  │(Rule Eng)│ │(OpenSrch│ │ (Kibana UI) │  │  │
│                    │  └──────────┘ └─────────┘ └─────────────┘  │  │
│                    │           192.168.1.130                     │  │
│                    └────────────────────────────────────────────┘  │
└─────────────────────────────────────────────────────────────────────┘

Detection Flow:
Host Events  ──► Wazuh Agent ──► Wazuh Manager ──► Indexer ──► Dashboard
Network Traffic ──► Suricata ──► EVE JSON ──────► Wazuh Manager ──────►┘
```

**Full architecture diagram and design rationale:** [docs/architecture.md](./docs/architecture.md)

---

## 🔭 Detection Coverage

### Coverage by Attack Category

| Category | Attacks Tested | Detected | Tools | Primary Rules |
|---|:---:|:---:|---|---|
| 🔑 **Authentication Attacks** | 2 | ✅ 2/2 | Hydra | Wazuh 5763, 100100 |
| 🌐 **Network Reconnaissance** | 2 | ✅ 2/2 | Nmap | Suricata sid:2010935 |
| 📁 **File Integrity & Tampering** | 3 | ✅ 3/3 | SSH/Bash/PS | Wazuh 550, 554, 2832 |
| 💀 **Malware & Process Exec** | 3 | ✅ 3/3 | PowerShell/NC | Wazuh 62138, 92057 |
| 💉 **Network & Injection Attacks** | 2 | ✅ 2/2 | SQLmap/hping3 | Suricata sid:2006446 |
| **TOTAL** | **12** | **✅ 12/12** | | |

### Alert Severity Distribution

```
Critical  ████████░░░░░░░░  1 alert  (SQL Injection — Level 1 Suricata)
High      ████████████████  2 alerts (SSH/RDP Brute Force — Wazuh Level 10)
Medium    ████████████░░░░  4 alerts (Port Scan, Cron, PS Exec)
Low/Info  ████████░░░░░░░░  5 alerts (FIM, EICAR, ICMP)
```

---

## 📊 Attack Validation Report

> Full details with screenshots and forensic notes: [reports/attack-validation-report.md](./reports/attack-validation-report.md)

### Summary Table

| # | Attack Scenario | Tool | Rule Fired | Severity | Status |
|:---:|---|---|---|:---:|:---:|
| 1 | SSH Brute-Force | Hydra | Wazuh `5763` | 🔴 High (L10) | ✅ |
| 2 | RDP Brute-Force | Hydra | Wazuh `100100` | 🔴 High (L10) | ✅ |
| 3 | TCP SYN Port Scan | Nmap -sS | Suricata `sid:2010935` → `86601` | 🟡 Medium | ✅ |
| 4 | Service Version Enum | Nmap -sV | Suricata `sid:2010935` → `86601` | 🟡 Medium | ✅ |
| 5 | Unauthorized `/etc/passwd` Mod | SSH/Bash | Wazuh `550` | 🟠 Suspicious (L7) | ✅ |
| 6 | Web Shell Upload | PowerShell | Wazuh `554` | 🟡 Medium (L5) | ✅ |
| 7 | Malicious Cron Job | SSH/Bash | Wazuh `2832` | 🟡 Medium (L5) | ✅ |
| 8 | EICAR Test File | PowerShell | Wazuh `62138` | 🟢 Low (L3) | ✅ |
| 9 | Encoded PowerShell | PS -EncodedCommand | Wazuh `92057`, `92213` | 🔴 High (L12) | ✅ |
| 10 | Netcat C2 / IRC | Netcat | Suricata `86601` (ET CHAT) | 🟡 Medium (L3) | ✅ |
| 11 | SQL Injection (DVWA) | SQLmap | Suricata `sid:2006446` → `86601` | 🔴 Critical (L1) | ✅ |
| 12 | ICMP Flood & Ping Sweep | hping3/Nmap | Suricata `sid:2100366` → `86601` | 🟢 Info (L3) | ✅ |

---

## 🗺️ MITRE ATT&CK Mapping

> Full mapping with sub-techniques and tuning notes: [docs/mitre-attack-mapping.md](./docs/mitre-attack-mapping.md)

| Test | Tactic | Technique | Sub-Technique | ID |
|---|---|---|---|---|
| SSH Brute Force | Credential Access | Brute Force | Password Guessing | T1110.001 |
| RDP Brute Force | Credential Access | Brute Force | Password Spraying | T1110.003 |
| TCP SYN Scan | Discovery | Network Service Discovery | — | T1046 |
| Version Enum | Discovery | Network Service Discovery | — | T1046 |
| `/etc/passwd` Mod | Persistence | Create Account | Local Account | T1136.001 |
| Web Shell Upload | Persistence | Server Software Component | Web Shell | T1505.003 |
| Cron Job | Persistence | Scheduled Task/Job | Cron | T1053.003 |
| EICAR / Malware | Execution | User Execution | Malicious File | T1204.002 |
| Encoded PowerShell | Defense Evasion | Obfuscated Files or Info | Command Obfuscation | T1027.010 |
| Netcat C2 | Command & Control | Application Layer Protocol | — | T1071 |
| SQL Injection | Initial Access | Exploit Public-Facing App | — | T1190 |
| ICMP Flood | Impact | Network Denial of Service | — | T1498 |

---

## ⚙️ Detection Rules

### Wazuh Custom Rules — `rules/wazuh/local_rules.xml`

Key custom and tuned rules deployed in this lab:

```xml
<!-- SSH Brute Force (Enhanced) -->
<rule id="100001" level="10" frequency="8" timeframe="60">
  <if_matched_sid>5710</if_matched_sid>
  <description>SSH brute force: 8+ failures in 60 seconds</description>
  <mitre><id>T1110.001</id></mitre>
  <group>authentication_failures,brute_force</group>
</rule>

<!-- RDP Attack Detection -->
<rule id="100100" level="10">
  <if_group>windows</if_group>
  <field name="win.system.eventID">^4625$</field>
  <description>RDP Attack Detected</description>
  <mitre><id>T1110.003</id></mitre>
  <group>rdp,authentication_failures</group>
</rule>

<!-- Encoded PowerShell Execution -->
<rule id="100200" level="12">
  <if_sid>92057</if_sid>
  <field name="win.eventdata.commandLine">-[Ee]nc</field>
  <description>Suspicious Base64-encoded PowerShell command executed</description>
  <mitre><id>T1027.010</id></mitre>
  <group>powershell,obfuscation</group>
</rule>
```

📄 Full rule file: [rules/wazuh/local_rules.xml](./rules/wazuh/local_rules.xml)

---

### Suricata Custom Rules — `rules/suricata/custom.rules`

```
# SSH Brute Force Detection
alert tcp any any -> $HOME_NET 22 (msg:"ET SCAN Potential SSH Brute Force"; \
  flow:to_server; threshold:type threshold,track by_src,count 10,seconds 60; \
  classtype:attempted-admin; sid:9000001; rev:1; metadata:mitre T1110.001;)

# SQL Injection UNION SELECT
alert http $EXTERNAL_NET any -> $HTTP_SERVERS $HTTP_PORTS \
  (msg:"ET WEB_SERVER SQL Injection UNION SELECT Attempt"; \
  flow:established,to_server; \
  content:"UNION"; nocase; http_uri; content:"SELECT"; nocase; http_uri; \
  classtype:web-application-attack; sid:2006446; rev:7; metadata:mitre T1190;)

# C2 IRC Beaconing
alert tcp $HOME_NET any -> $EXTERNAL_NET 6667 \
  (msg:"ET CHAT IRC C2 Channel Communication"; \
  flow:established,to_server; content:"NICK"; content:"USER"; \
  classtype:policy-violation; sid:9000003; rev:1; metadata:mitre T1071;)
```

📄 Full rule file: [rules/suricata/custom.rules](./rules/suricata/custom.rules)

---

## 🖥️ Lab Environment

| Component | Specification |
|---|---|
| **Wazuh Server** | Ubuntu Server 24.04 · Wazuh Manager + Indexer + Dashboard |
| **Suricata Sensor** | Ubuntu Server 24.04 · Suricata 8.0.3 · 48,919 ET rules loaded |
| **Linux Endpoint** | Ubuntu Server · Wazuh Agent · IP: 192.168.1.151 |
| **Windows Endpoint** | Windows Server · Wazuh Agent · Sysmon configured |
| **Attacker Machine** | Kali Linux 2025.4 · IP: 192.168.1.131 |
| **Vulnerable App** | DVWA (Apache + PHP + MariaDB) |
| **Network Mode** | Host-Only / Internal · Isolated lab segment |
| **Hypervisor** | VMware / VirtualBox (reproducible OVA) |

### Tools Used in Attack Simulation

```
Hydra v9.5      — Brute force authentication attacks
Nmap 7.95       — Port scanning & service enumeration  
SQLmap 1.8      — Automated SQL injection
hping3 3.0      — Packet crafting & ICMP flooding
Netcat 1.10     — C2 channel simulation
PowerShell 7    — Malware simulation & encoded commands
Metasploit      — Post-exploitation framework (reference)
```

---

## 🚀 Quick Start

### Prerequisites

```bash
# Minimum hardware per VM
CPU: 2 cores  |  RAM: 4GB  |  Disk: 40GB
```

### 1. Deploy Wazuh Server

```bash
# One-line installer (official Wazuh method)
curl -sO https://packages.wazuh.com/4.x/wazuh-install.sh
bash wazuh-install.sh -a

# Verify services
systemctl status wazuh-manager wazuh-indexer wazuh-dashboard
```

### 2. Deploy Suricata

```bash
sudo apt update && sudo apt install -y suricata
sudo suricata-update                    # Pull Emerging Threats ruleset

# Apply our custom config
sudo cp configs/suricata.yaml /etc/suricata/suricata.yaml
sudo systemctl restart suricata
```

### 3. Load Custom Detection Rules

```bash
# Wazuh rules
sudo cp rules/wazuh/local_rules.xml /var/ossec/etc/rules/local_rules.xml
sudo /var/ossec/bin/wazuh-logtest      # Validate rules

# Suricata rules  
sudo cp rules/suricata/custom.rules /etc/suricata/rules/custom.rules
sudo suricata -T -c /etc/suricata/suricata.yaml   # Test config
sudo systemctl reload suricata
```

### 4. Configure Log Forwarding

```bash
# Forward Suricata EVE JSON to Wazuh
sudo cp configs/wazuh-agent.conf /var/ossec/etc/ossec.conf
sudo systemctl restart wazuh-agent
```

### 5. Verify Integration

```bash
# Watch live alerts
sudo tail -f /var/ossec/logs/alerts/alerts.json | python3 -m json.tool

# Dashboard: https://<wazuh-server>:443
# Default creds: admin / admin (change immediately!)
```

📘 Detailed setup guide: [docs/setup-guide.md](./docs/setup-guide.md)

---

## 📁 Repository Structure

```
siem-ids-detection-lab/
│
├── 📄 README.md                        ← You are here
│
├── 📂 rules/
│   ├── 📂 wazuh/
│   │   └── local_rules.xml             ← 15+ custom Wazuh detection rules
│   └── 📂 suricata/
│       └── custom.rules                ← Custom Suricata signatures
│
├── 📂 configs/
│   ├── suricata.yaml                   ← Tuned Suricata config
│   └── wazuh-agent.conf                ← Agent + EVE JSON forwarding config
│
├── 📂 docs/
│   ├── architecture.md                 ← Full network/system design
│   ├── setup-guide.md                  ← Step-by-step deployment
│   ├── mitre-attack-mapping.md         ← ATT&CK coverage matrix
│   ├── tuning-methodology.md           ← Rule tuning & FP reduction notes
│   └── soc-analyst-playbook.md         ← Analyst response procedures
│
├── 📂 reports/
│   └── attack-validation-report.md     ← 12 test cases with results
│
├── 📂 scripts/
│   ├── deploy-wazuh.sh                 ← Automated Wazuh deployment
│   ├── test-attacks.sh                 ← Attack simulation helper scripts
│   └── validate-rules.sh              ← Rule validation utility
│
└── 📂 .github/
    └── CONTRIBUTING.md
```

---

## 📈 Results & Findings

### Detection Performance

```
┌─────────────────────────────────────────────────────────┐
│  DETECTION RESULTS          12/12 Tests Passed (100%)  │
├─────────────────────────────────────────────────────────┤
│  Auth Attacks        ████████████████  2/2  ✅          │
│  Network Recon       ████████████████  2/2  ✅          │
│  File Integrity      ████████████████  3/3  ✅          │
│  Malware/Process     ████████████████  3/3  ✅          │
│  Network/Injection   ████████████████  2/2  ✅          │
└─────────────────────────────────────────────────────────┘
```

### Key Takeaways

**✅ What Worked Well**
- Wazuh FIM caught unauthorized `/etc/passwd` modification within **< 5 seconds**
- Suricata detected SQL injection with **4 bonus rule hits** beyond the expected SID (depth of detection)
- Encoded PowerShell triggered **Level 12** alert — correctly flagged as high priority
- Suricata absorbed **189MB / 3.15M packets** in the ICMP flood test with no service degradation

**⚠️ Interesting Deviations**
- EICAR test file triggered rule `62138` instead of the expected `87105` — Wazuh detected via FIM before VirusTotal integration fired; both outcomes are valid detection paths
- ICMP flood triggered `sid:2100366` (GPL ICMP PING \*NIX) instead of the expected `2100469` — actual behavior of the Emerging Threats ruleset differed from documentation; adapted response accordingly

**📌 Tuning Insights**
- SSH alert threshold tuned from 3→8 failures to reduce FP noise on development environments
- Suricata `EXTERNAL_NET: any` required for LAN-internal testing (not recommended for production)
- Windows Event ID 4625 + RDP port correlation significantly reduces false positives on shared servers

---

## 👥 Team

| Name | Student ID | Role |
|---|---|---|
| **Loch Thida** | IDTB100005 | Infrastructure Lead & Wazuh Configuration |
| **Khao Vandoeun** | IDTB100174 | Suricata / Network Detection & Rule Authoring |
| **Thy Dayuth** | IDTB100355 | Attack Simulation, Testing & Documentation |

**Supervisor:** Professor Chan Oeurn  
**Institution:** Cambodia Academy of Digital Technology (CADT)  
**Program:** Bachelor of Cybersecurity  
**Department:** Telecommunications and Networking  

---

## 📚 References

- [Wazuh Documentation](https://documentation.wazuh.com/)
- [Suricata Documentation](https://docs.suricata.io/)
- [Emerging Threats Ruleset](https://rules.emergingthreats.net/)
- [MITRE ATT&CK Framework](https://attack.mitre.org/)
- [DVWA — Damn Vulnerable Web Application](https://github.com/digininja/DVWA)
- [Wazuh Rule Syntax Reference](https://documentation.wazuh.com/current/user-manual/ruleset/ruleset-xml-syntax/rules.html)

---

## 📄 License

This project is licensed under the **MIT License** — see [LICENSE](./LICENSE) for details.

The detection rules in `/rules/` are released for educational and research use. The Emerging Threats ruleset is subject to its own [licensing terms](https://rules.emergingthreats.net/OPEN_download_instructions.html).

---

<div align="center">

**⭐ If this helped you, give the repo a star!**

*Built with 🔒 at Cambodia Academy of Digital Technology · 2026*

</div>
=======
# Procedure and Guideline For Implementation


>>>>>>> 98dcc8b0b15115fdbdfe1bbb55892e169af5d481
