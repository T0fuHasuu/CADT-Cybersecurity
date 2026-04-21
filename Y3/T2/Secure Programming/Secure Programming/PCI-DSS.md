## PCI-DSS 3-Month Implementation Plan

### Hardware Role Assignment

|Device|Role|Zone|
|---|---|---|
|PC (Win11)|Admin workstation / Documentation|Out-of-Scope|
|T495 (Arch)|CDE Server (simulated payment processing)|In-Scope CDE|
|X230 (Arch/Debian)|Security tools (monitoring, scanning, logging)|Security/DMZ|
|TP-Link WR940N|Network boundary / perimeter|Perimeter|

---

## Phase 1 — Foundation (Week 1–2)

**Goal: Scope, design, and document before touching anything**

- Define CDE scope: T495 is your CDE, everything else is out-of-scope or DMZ
- Draw network diagram and data flow diagram (satisfies Req 1.2.3 & 1.2.4)
- Document IP scheme:
    - `192.168.10.x` → CDE (T495)
    - `192.168.20.x` → Security/DMZ (X230)
    - `192.168.30.x` → Out-of-scope (PC)
- Set up T495 as software router/firewall between zones using `nftables` since your TP-Link WR940N doesn't support VLANs natively
- Create your evidence folder structure from day one

**Deliverables:** Network diagram, data flow diagram, scope document, IP scheme

---

## Phase 2 — Network Security & Hardening (Week 3–4)

**Goal: Req 1 & 2**

- Configure `nftables` on T495 as CDE gateway with strict inbound/outbound rules
- Configure TP-Link: change defaults, disable unnecessary services, set strong admin password
- Apply CIS benchmark hardening on all three machines:
    - Remove unused packages/services
    - Disable root SSH login
    - Set password policies via PAM
- Document configuration standards for each system

**Tools:** `nftables`, `ssh`, `PAM`, `systemd`, manual CIS checklist

**Deliverables:** Firewall ruleset, hardening checklist, configuration standards doc

---

## Phase 3 — Data Protection (Week 5–6)

**Goal: Req 3 & 4**

- Encrypt CDE storage on T495 using LUKS (already done at install or re-partition)
- Set up a simulated card data database (PostgreSQL) with column-level encryption using `pgcrypto`
- Configure TLS 1.2/1.3 only for all services on T495 using OpenSSL
- Key management: document key lifecycle, store keys separately, use GPG for key protection
- Write data retention and disposal policy

**Tools:** `LUKS`, `PostgreSQL + pgcrypto`, `OpenSSL`, `stunnel` for encrypting legacy connections

**Deliverables:** Encryption implementation doc, key management procedure, TLS config evidence

---

## Phase 4 — Access Control & Vulnerability Management (Week 7–8)

**Goal: Req 5, 6, 7, 8**

- Install ClamAV on T495 and X230, configure scheduled scans
- Set up OpenVAS/Greenbone on X230 for vulnerability scanning
- Implement least-privilege user accounts on all systems
- Configure `sudo` with minimal permissions
- Set up MFA using Google Authenticator PAM module for SSH into T495
- Write patch management procedure and apply all pending updates
- Set up a simple application (Python Flask or similar) on T495 to simulate payment processing with input validation

**Tools:** `ClamAV`, `OpenVAS/Greenbone`, `google-authenticator-libpam`, `sudo`, `apt/pacman`

**Deliverables:** Antivirus scan logs, vulnerability scan report, user access matrix, MFA config evidence

---

## Phase 5 — Monitoring, Logging & Intrusion Detection (Week 9–10)

**Goal: Req 10, 11**

- Set up centralized logging: `rsyslog` on T495 and X230 forwarding to X230 as log server
- Install Graylog or ELK Stack on X230 for log aggregation and review (use Graylog if RAM is tight)
- Configure `auditd` on T495 to log all file access, user activity, privilege escalation
- Install Snort or Suricata on X230 for IDS
- Install AIDE on T495 for file integrity monitoring
- Set up daily log review procedure (document it)
- Configure NTP sync on all machines to a common time source

**Tools:** `rsyslog`, `auditd`, `Graylog`, `Snort/Suricata`, `AIDE`, `chrony/ntpd`

**Deliverables:** Log samples, IDS config, FIM baseline, log review procedure, daily monitoring evidence

---

## Phase 6 — Testing, Policy & Audit Prep (Week 11–12)

**Goal: Req 9, 11, 12 + Final audit readiness**

- Run internal vulnerability scan with OpenVAS, document findings and remediation
- Basic penetration test using Nmap, Nikto, and Metasploit against your own CDE (T495)
- Document physical access controls (even if simulated — who has physical access, locked room, etc.)
- Write all required policies:
    - Information Security Policy
    - Incident Response Plan
    - Acceptable Use Policy
    - Change Management Procedure
    - Third-Party Service Provider Policy
- Compile all evidence per requirement
- Do a self-assessment walkthrough using PCI-DSS SAQ-D as your checklist
- Fix any remaining gaps

**Deliverables:** Pen test report, all policy documents, complete evidence binder, SAQ-D completed

---

## Requirement Coverage Map

|Req|What You're Doing|
|---|---|
|1|nftables firewall on T495, TP-Link perimeter config|
|2|CIS hardening, no defaults on all devices|
|3|LUKS + pgcrypto, data retention policy|
|4|TLS 1.2/1.3, OpenSSL, stunnel|
|5|ClamAV on CDE and DMZ|
|6|OpenVAS scanning, patch management, secure app dev|
|7|Least privilege, sudo matrix|
|8|Unique accounts, PAM password policy, MFA via SSH|
|9|Physical access documentation|
|10|auditd, rsyslog, Graylog, daily log reviews|
|11|AIDE (FIM), Snort/Suricata (IDS), Nmap/Nikto/Metasploit (pen test)|
|12|All policies, incident response, evidence collection|

---

## Quick Notes

- Keep an **evidence log daily** — screenshots, config exports, scan reports. This is what passes audits.
- Your simulated environment is valid for audit purposes as long as you document the **scope clearly** and treat T495 as if it were a real production CDE.
- The SAQ-D self-assessment is your audit checklist — complete it at the end of each phase, not just at the end.
- Physical access for Req 9 can be handled with a simple written policy about who accesses the machines.