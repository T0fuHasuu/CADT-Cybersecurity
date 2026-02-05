1. Identity and Access Management (IAM)
2. Network Security Architecture
3. Perimeter Defence and Threat Detection
4. Data Security and Encryption
5. Endpoint Security
6. Cloud Security Architecture7. Application Security
8. Security Operations and Monitoring9. Governance, Risk, and Compliance (GRC)
9. Governance, Risk, and Compliance (GRC)
10. Business Continuity and Disaster Recovery

This is a critical question for your "Asset Analysis." If you misunderstand what "30 applications" means, your AppLocker strategy (Section 4.5) will fail.

To answer directly: **Yes, all 30 are "in use," but NOT by every single person.**

In a Telecom company, "Application" doesn't just mean software you install like Microsoft Word. It includes **Web Portals** (billing systems), **Background Tools** (databases), and **Mobile Apps** (for field techs).

Here is the **realistic breakdown** of those 30 applications to help you fill out your "Asset Inventory" and "Whitelisting" sections.

---

### **The Breakdown of the "30 Applications"**

You should categorize them in your report to show the professor you understand the industry.

#### **Category A: Corporate Productivity (Used by ~450 Employees)**
*These are the standard apps installed on almost every laptop (HR, Sales, Marketing).*
1.  **Microsoft 365 Apps** (Word, Excel, PowerPoint, Outlook)
2.  **Microsoft Teams** (Communication)
3.  **Google Chrome / Edge** (Web Browsers)
4.  **Adobe Acrobat Reader** (PDFs)
5.  **Zoom** (Video Conferencing - Backup to Teams)
6.  **SAP Concur** (Expense Reporting - usually web-based but considered an "App")
7.  **Workday** (HR Portal)

#### **Category B: Core Telecom Business (BSS) (Used by Sales & Support)**
*BSS = Business Support Systems. These are critical for money.*
8.  **Salesforce** (CRM - Customer Relationship Management)
9.  **Amdocs Billing Suite** (The software that generates phone bills)
10. **Call Center Softphone** (Cisco Jabber or Avaya - allows calling from the laptop)
11. **Ticketing System** (ServiceNow or Jira - for tracking customer complaints)

#### **Category C: Network Operations (OSS) (Used by ~50 Engineers)**
*OSS = Operations Support Systems. These control the cell towers.*
12. **SolarWinds** (Network Monitoring)
13. **Putty** (SSH tool to connect to servers/routers)
14. **Wireshark** (Packet analyzer for troubleshooting)
15. **Python** (Scripting language for automation)
16. **Ericsson Network Manager** (Specific tool for 5G tower management)
17. **FileZilla** (FTP for moving configuration files)

#### **Category D: Contractor & Vendor Tools (Used by ~50 Contractors)**
*This is your "High Risk" category.*
18. **Citrix Workspace** (Virtual Desktop for remote vendors)
19. **VPN Client** (Cisco AnyConnect or OpenVPN)
20. **Vendor Portal** (A specific web app where vendors upload invoices)

#### **Category E: Security & Infrastructure (Used by IT/Background)**
*These run silently or are used by Admins.*
21. **Wazuh Agent** (Your EDR)
22. **Tactical RMM Agent** (Your Patcher)
23. **Active Directory Admin Center**
24. **Powershell** (Built-in, but must be managed)
25. **Backup Agent** (Veeam)

*(Apps 26-30 are typically legacy utilities like Java, 7-Zip, Notepad++, etc.)*

---

### **How this affects your Security Strategy**

When you write **Section 4.5 (Application Whitelisting)**, you must clarify that **not everyone gets every app**.

* **The Problem:** If you allow "Wireshark" (a packet sniffer) on a Salesperson's laptop, a hacker could use it to steal passwords.
* **The Solution (for your report):**
    * **Group A (Sales/HR):** Whitelist only *Category A* + *Category B*. **Block** *Category C*.
    * **Group B (Engineers):** Allow *Category C*.
    * **Group C (Contractors):** **Block everything** except *Category D* (Citrix/Web Browser). They should not have local apps installed.

**Summary for your Report:**
> *"The '30 Applications' represent the total authorized software catalog. However, based on the Principle of Least Privilege, no single user will have access to all 30. Endpoint policies will restrict 'Network Tools' solely to the Engineering User Group."*




1. Implementation Results: What You Will See (The "Deliverables")

When you present this, you are describing the Day 1 State of the system after a successful deployment.

A. Wazuh (The Security Dashboard)

After deploying the agent to 550 devices, your Wazuh Dashboard will light up with data.

    The "Security Events" Screen: You will see a live feed of "Alerts."

        Result: A pie chart showing "Top 5 Agents with High Severity Alerts."

        Specific Log Example: You will see an entry like: Rule: 554 - File Integrity Monitoring - File modified: C:\Windows\System32\drivers\etc\hosts.

        Outcome: You know exactly which laptop has a potential malware infection attempting to redirect DNS.

    Vulnerability Report:

        Result: A list of all 550 endpoints ranked by "Vulnerability Score."

        Specific Data: "Endpoint: HR-Laptop-04 | CVE-2023-21716 (Critical) | Microsoft Word RCE | Status: Active."

        Outcome: You have a prioritized "To-Do" list for patching.

B. Tactical RMM (The Command Center)

This is your daily operations screen.

    The "Patch Matrix":

        Result: A grid showing all 500 employee laptops. Green dots = Fully Patched. Red dots = Missing critical updates.

        Outcome: You can prove to auditors that 98% of devices are compliant.

    Remote Access:

        Result: Right-clicking any laptop in the list gives you a "Remote Background" shell.

        Outcome: You can fix a user's printer issue via command line without interrupting their work.

C. AppLocker (The "Shield")

AppLocker runs silently. You don't see a dashboard; you see logs and user behavior.

    The "Block" Experience:

        Result: A user tries to run minecraft_installer.exe they downloaded. Windows displays a blue popup: "This app has been blocked by your system administrator."

        Log Generated: Event Viewer ID 8004: %OSDRIVE%\Users\JohnDoe\Downloads\minecraft.exe was prevented from running.

        Outcome: Zero-day ransomware executables are blocked by default because they are not on the "White List."

D. Mobile Devices (Headwind MDM)

    The "Kiosk" Screen:

        Result: The 50 Field Tablets display only the 4 apps you allowed (Maps, TicketSystem, Camera, Settings). There is no "Home" button to go back to the Android menu.

        Outcome: Technicians cannot install games or watch Netflix on company data.