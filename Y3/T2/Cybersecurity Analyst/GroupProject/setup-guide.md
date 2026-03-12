# 🚀 Setup Guide

Step-by-step deployment for the full Wazuh + Suricata lab environment.

---

## Prerequisites

### Hardware (per VM)
| Role | CPU | RAM | Disk |
|---|---|---|---|
| Wazuh Server | 4 cores | 8GB | 50GB |
| Suricata Sensor | 2 cores | 4GB | 30GB |
| Linux Endpoint | 2 cores | 2GB | 20GB |
| Windows Endpoint | 2 cores | 4GB | 40GB |
| Attacker (Kali) | 2 cores | 4GB | 40GB |

### Software
- VMware Workstation / VirtualBox / Proxmox
- Ubuntu Server 24.04 LTS ISO
- Windows Server 2022 ISO
- Kali Linux 2025.x ISO

---

## Phase 1: Wazuh Server Deployment

### 1.1 Install Wazuh (All-in-One)

```bash
# Download and run the Wazuh installer
curl -sO https://packages.wazuh.com/4.x/wazuh-install.sh
curl -sO https://packages.wazuh.com/4.x/config.yml

# Edit config.yml with your server IP
nano config.yml

# Run installation
bash wazuh-install.sh -a -i

# Save the output! It contains admin credentials.
```

### 1.2 Verify Services

```bash
systemctl status wazuh-manager    # Should be: active (running)
systemctl status wazuh-indexer     # Should be: active (running)
systemctl status wazuh-dashboard   # Should be: active (running)
```

### 1.3 Access Dashboard

Navigate to: `https://<wazuh-server-ip>` (ignore SSL warning in lab)

Default credentials are printed during installation. **Change immediately.**

### 1.4 Load Custom Rules

```bash
# Copy custom Wazuh rules
sudo cp rules/wazuh/local_rules.xml /var/ossec/etc/rules/local_rules.xml

# Validate rule syntax
sudo /var/ossec/bin/wazuh-logtest

# Restart manager to load new rules
sudo systemctl restart wazuh-manager
```

---

## Phase 2: Suricata Deployment

### 2.1 Install Suricata

```bash
sudo add-apt-repository ppa:oisf/suricata-stable -y
sudo apt update
sudo apt install -y suricata

# Verify version
suricata --build-info | grep "Version"
```

### 2.2 Update Rules (Emerging Threats)

```bash
sudo suricata-update

# List available rule sources
sudo suricata-update list-sources

# Enable additional sources (optional)
sudo suricata-update enable-source et/open
sudo suricata-update update
```

### 2.3 Add Custom Rules

```bash
sudo cp rules/suricata/custom.rules /etc/suricata/rules/custom.rules

# Add to suricata.yaml rule-files section
sudo nano /etc/suricata/suricata.yaml
# Add:  - custom.rules

# Test configuration
sudo suricata -T -c /etc/suricata/suricata.yaml -v
```

### 2.4 Apply Lab Configuration

```bash
# Backup original config
sudo cp /etc/suricata/suricata.yaml /etc/suricata/suricata.yaml.bak

# Apply our config
sudo cp configs/suricata.yaml /etc/suricata/suricata.yaml

# Start on correct interface
sudo suricata -D -i eth0 -c /etc/suricata/suricata.yaml
# OR use systemd:
sudo systemctl start suricata
sudo systemctl enable suricata
```

### 2.5 Verify EVE JSON Output

```bash
# Watch live alerts
sudo tail -f /var/log/suricata/eve.json | python3 -m json.tool
```

---

## Phase 3: Wazuh Agent Deployment

### 3.1 Linux Agent

```bash
# On the Linux endpoint
curl -s https://packages.wazuh.com/key/GPG-KEY-WAZUH | sudo apt-key add -
echo "deb https://packages.wazuh.com/4.x/apt/ stable main" | \
  sudo tee /etc/apt/sources.list.d/wazuh.list
sudo apt update && sudo apt install -y wazuh-agent

# Configure manager address
sudo sed -i 's/MANAGER_IP/<your-wazuh-manager-ip>/' /var/ossec/etc/ossec.conf

# Apply our custom config (adds Suricata EVE JSON forwarding)
sudo cp configs/wazuh-agent.conf /var/ossec/etc/ossec.conf

# Start and register agent
sudo systemctl enable wazuh-agent
sudo systemctl start wazuh-agent
```

### 3.2 Windows Agent

```powershell
# Download agent installer from Wazuh Manager dashboard
# Or use PowerShell:
Invoke-WebRequest -Uri "https://packages.wazuh.com/4.x/windows/wazuh-agent-4.x.x-1.msi" `
  -OutFile "wazuh-agent.msi"

# Silent install
msiexec.exe /i wazuh-agent.msi /q `
  WAZUH_MANAGER="192.168.1.130" `
  WAZUH_AGENT_GROUP="default"

# Start service
NET START WazuhSvc
```

### 3.3 Install Sysmon (Windows)

```powershell
# Download Sysmon and SwiftOnSecurity config (recommended)
Invoke-WebRequest -Uri "https://live.sysinternals.com/Sysmon64.exe" -OutFile "Sysmon64.exe"
Invoke-WebRequest -Uri "https://raw.githubusercontent.com/SwiftOnSecurity/sysmon-config/master/sysmonconfig-export.xml" `
  -OutFile "sysmonconfig.xml"

# Install
.\Sysmon64.exe -accepteula -i sysmonconfig.xml
```

---

## Phase 4: Verify Integration

### 4.1 Check Agent Connectivity

```bash
# On Wazuh Manager
sudo /var/ossec/bin/agent_control -l
# Should show all agents as "Active"
```

### 4.2 Test Detection Pipeline

```bash
# Generate a test SSH failure (from attacker machine)
ssh nonexistentuser@192.168.1.151

# Check Wazuh receives the alert
sudo tail -f /var/ossec/logs/alerts/alerts.json | grep -i "ssh\|brute"
```

### 4.3 Verify Suricata → Wazuh Flow

```bash
# Generate test traffic (Nmap ping sweep from attacker)
sudo nmap -sn 192.168.1.0/24

# Check Suricata EVE log
sudo tail -f /var/log/suricata/eve.json | python3 -m json.tool | grep -i "alert"

# Check Wazuh dashboard for Suricata alerts
# Dashboard → Events → Search: data.alert.signature:*
```

---

## Troubleshooting

| Issue | Check | Fix |
|---|---|---|
| Agent not connecting | `systemctl status wazuh-agent` | Check firewall: port 1514 open? |
| Suricata not detecting | `suricata -T -c /etc/suricata/suricata.yaml` | Config syntax error; check output |
| EVE JSON not in Wazuh | Wazuh agent log: `/var/ossec/logs/ossec.log` | Check localfile path in ossec.conf |
| Dashboard not loading | `systemctl status wazuh-dashboard` | Wait 2-3 min after start; check memory |
| Rules not firing | `/var/ossec/bin/wazuh-logtest` | Test rule with sample log line |

---

*Setup guide version 1.0 · March 2026*
