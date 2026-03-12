# 🏗️ Architecture & Design

## Overview

The lab simulates a small enterprise SOC where a security analyst monitors a local network using open-source tools. The design follows a **defense-in-depth** approach combining host-based (HIDS) and network-based (NIDS) detection.

---

## Network Topology

```
                    ┌─────────────────────┐
                    │   Attacker Machine  │
                    │   Kali Linux 2025.4 │
                    │   192.168.1.131     │
                    └──────────┬──────────┘
                               │  Attack Traffic
                    ┌──────────┴──────────────────────────────────┐
                    │              Internal LAN                    │
                    │           192.168.1.0/24                     │
                    └──┬──────────────┬──────────────┬────────────┘
                       │              │              │
           ┌───────────▼──┐  ┌────────▼────┐  ┌────▼──────────────────┐
           │ Linux Endpoint│  │Win Endpoint │  │   Wazuh Server /      │
           │ Ubuntu 24.04  │  │ Windows Srv │  │   Suricata Sensor     │
           │ 192.168.1.151 │  │             │  │   192.168.1.130       │
           │ Wazuh Agent   │  │ Wazuh Agent │  │                       │
           └───────┬───────┘  └──────┬──────┘  │ ┌─────────────────┐  │
                   │                 │          │ │  Wazuh Manager  │  │
                   │  Agent Logs     │          │ │  Wazuh Indexer  │  │
                   └─────────────────┴──────────► │  Wazuh Dashboard│  │
                                                │ └─────────────────┘  │
                                   Network ────►│ ┌─────────────────┐  │
                                   Traffic      │ │  Suricata 8.0.3 │  │
                                                │ │  48,919 rules   │  │
                                                │ └─────────────────┘  │
                                                └───────────────────────┘
```

---

## Component Roles

### Wazuh Server (`192.168.1.130`)

The central hub of the SOC environment. Runs three co-located components:

| Component | Port | Function |
|---|---|---|
| **Wazuh Manager** | 1514 (UDP/TCP) | Rule engine, agent comms, alert generation |
| **Wazuh Indexer** | 9200 | OpenSearch-based log storage & search |
| **Wazuh Dashboard** | 443 | Kibana-based visualization & analyst interface |

### Suricata Sensor (`192.168.1.130`)

Co-located with the Wazuh server in this lab (in production, deploy on a dedicated tap/span node):

- Inspects all mirrored traffic on the internal interface
- Outputs alerts in **EVE JSON** format to `/var/log/suricata/eve.json`
- Wazuh agent reads EVE JSON and forwards to Wazuh Manager for unified correlation

### Wazuh Agents (Linux + Windows Endpoints)

Lightweight agents installed on monitored endpoints:

- Collect: System logs, auth logs, Windows Event Logs, Sysmon events
- Monitor: File Integrity (FIM) on critical paths
- Report: Near real-time to Wazuh Manager over encrypted channel (port 1514)

---

## Data Flow

```
┌─────────────┐     ┌──────────────────────────────────────────────────────┐
│  Endpoints  │     │                  Wazuh Server                        │
│             │     │                                                      │
│ Auth Logs   ├────►│  ┌────────────┐   ┌──────────────┐   ┌──────────┐  │
│ Sysmon Logs │     │  │   Wazuh    │   │    Wazuh     │   │  Wazuh   │  │
│ Windows EVT │     │  │  Manager   ├──►│   Indexer    ├──►│Dashboard │  │
│ FIM Events  │     │  │(Rule Engine│   │(OpenSearch)  │   │(Analyst  │  │
│             │     │  │            │   │              │   │   UI)    │  │
└─────────────┘     │  └─────▲──────┘   └──────────────┘   └──────────┘  │
                    │        │                                             │
┌─────────────┐     │  ┌─────┴──────┐                                     │
│  Network    │     │  │  Suricata  │                                     │
│  Traffic    ├────►│  │ EVE JSON   │                                     │
│(all packets)│     │  │  Alerts    │                                     │
└─────────────┘     │  └────────────┘                                     │
                    └──────────────────────────────────────────────────────┘
```

---

## Wazuh Alert Severity Model

| Level Range | Severity | Description | Action |
|:---:|---|---|---|
| 0–6 | Informational | Debug/audit events | No action required |
| 7–9 | Low | Single failed login, minor events | Monitor & log |
| 10–12 | Medium | Repeated failures, scan patterns | Investigate |
| 13–15 | High/Critical | Confirmed intrusion, file tampering | Immediate response |

## Suricata Priority Model

| Priority | Severity | Examples |
|:---:|---|---|
| 1 | Critical | Active exploitation, confirmed attack |
| 2 | Medium | Suspicious activity, policy violations |
| 3 | Low/Informational | Scans, probes, noise |

---

## Integration: Suricata → Wazuh

EVE JSON log forwarding config in `ossec.conf`:

```xml
<localfile>
  <log_format>json</log_format>
  <location>/var/log/suricata/eve.json</location>
</localfile>
```

Wazuh reads Suricata's `eve.json` and maps fields to its decoder, then applies both built-in and custom rules for unified alert generation.

---

## Security Hardening Applied

- SSH root login disabled (`PermitRootLogin no`)
- Wazuh Manager API secured with TLS
- Dashboard HTTPS only (self-signed cert in lab)
- Agent-manager communication encrypted (port 1514)
- Firewall (ufw) restricts management interfaces to internal LAN only
