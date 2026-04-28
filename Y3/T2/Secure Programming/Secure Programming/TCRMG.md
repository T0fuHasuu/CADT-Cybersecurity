This study guide summarises the **National Bank of Cambodia’s (NBC) Technology and Cyber Risk Management Guidelines (TCRMG), January 2026**, focusing on essential lessons and mandatory compliance requirements for Banks and Financial Institutions (BFIs).

### **A. Document Overview**
The **TCRMG 2026** is a regulatory framework issued by the NBC to replace the 2019 version. It exists to provide a standard for managing the risks that come with **digital transformation**. Its primary goals are to ensure BFIs have a strong risk management framework, improve their ability to survive cyberattacks (**resilience**), and protect customer data while the industry grows.

---

### **B. & C. Chapter Summary, Purpose, and Key Requirements**

#### **Chapter 1: IT Governance**
*   **Purpose:** To create a clear chain of command and accountability for technology.
*   **Key Requirements:**
    *   The **Board** is ultimately responsible for IT strategy and risk oversight.
    *   BFIs **shall** document an IT organisation structure approved by **senior management**.
    *   BFIs **shall** appoint a **CITO** (Chief IT Officer) and **CISO** (Chief InfoSec Officer) or equivalent roles.
*   **Key Terms:** Board Support, CITO, CISO, Accountability.

#### **Chapter 2: Policies, Standards, and Procedures**
*   **Purpose:** To provide a written rulebook for how technology and security are managed.
*   **Key Requirements:**
    *   The **Board shall approve** IT Operation and Information Security policies.
    *   **Senior Management shall approve** lower-level standards and procedures.
    *   Policies **shall** be reviewed **annually**.
*   **Key Terms:** Policy (rules), Standard (compulsory specs), Procedure (step-by-step).

#### **Chapter 3: IT Operation Management**
*   **Purpose:** Managing daily tech activities like updates, changes, and physical safety.
*   **Key Requirements:**
    *   BFIs **shall** have a **Technology Risk Management Framework (TRMF)** integrated into their overall risk strategy.
    *   **Asset Inventory:** BFIs **shall** track all hardware, software, and data.
    *   **Patch & Change:** Updates **shall** be tested before deployment. **Emergency fixes** require specific controls.
    *   **Data Center:** BFIs **shall** have at least one **primary data center in Cambodia**.
*   **Key Terms:** TRMF, EOS (End of Support), SPOF (Single Point of Failure), RTO/RPO.

#### **Chapter 4: Cybersecurity Management**
*   **Purpose:** Defending the BFI from hackers and internal threats.
*   **Key Requirements:**
    *   **Access Control:** Use **Role-Based Access Control (RBAC)** and **Least Privilege** (give only the access needed).
    *   **VPN/Remote:** All remote access **shall** use **Two-Factor Authentication (2FA)**.
    *   **Testing:** Penetration testing **shall** be done **annually**; Red Teaming (attack simulations) **shall** be done **every two years**.
*   **Key Terms:** RBAC, 2FA, PQC (Post-Quantum Cryptography), Red Teaming.

#### **Chapter 5: Digital Service Protection**
*   **Purpose:** Protecting customers using apps, websites, and ATMs.
*   **Key Requirements:**
    *   **2FA shall** be used for all online/mobile banking logins and registrations.
    *   **SSTs (ATMs): shall** have anti-skimming devices and PIN pad shields.
    *   **Payment Cards:** **shall** comply with **PCI DSS** standards.
*   **Key Terms:** OTP (One-Time Password), Anti-skimming, PCI DSS.

#### **Chapter 6: Enabling Technologies (Cloud, AI, etc.)**
*   **Purpose:** Guidelines for using modern tech like Cloud and Artificial Intelligence.
*   **Key Requirements:**
    *   BFIs **shall** seek **NBC approval** before using these technologies for **critical systems**.
    *   **Cloud:** BFIs **shall** have a **Cloud Exit Strategy** to move data if the provider fails.
*   **Key Terms:** SaaS/PaaS/IaaS, Shared Responsibility Model, API, AI/ML.

#### **Chapter 7: Technology Service Outsourcing**
*   **Purpose:** Managing risks when hiring third-party companies to handle IT.
*   **Key Requirements:**
    *   Outsourcing **shall** be approved by the **Board**.
    *   Contracts **shall** include a **"Right to Audit"** for both the BFI and the **NBC**.
*   **Key Terms:** Due Diligence, SLA (Service Level Agreement), NDA, Right to Audit.

#### **Chapter 8: Business Continuity Management (BCM)**
*   **Purpose:** Planning how to keep the bank running if a disaster happens.
*   **Key Requirements:**
    *   **BIA:** BFIs **shall** conduct a Business Impact Analysis to find critical functions.
    *   **Testing:** BCP and DRP **shall** be tested regularly to ensure they work.
*   **Key Terms:** BIA, BCP (Business Continuity Plan), DRP (Disaster Recovery Plan).

#### **Chapter 9: Customer Personal Data Protection**
*   **Purpose:** Keeping customer info private and legal.
*   **Key Requirements:**
    *   **Consent:** BFIs **shall** get clear permission from customers before collecting data.
    *   **Storage:** Moving data **outside Cambodia** requires **prior NBC approval**.
*   **Key Terms:** Privacy by Design, Data Minimization, Consent.

#### **Chapter 10: Information Technology Audit**
*   **Purpose:** Independent checking to make sure all the above rules are being followed.
*   **Key Requirements:**
    *   BFIs **shall** have an **independent IT Audit function**.
    *   The **Audit Lead shall** have a professional certification and at least **3 years of experience**.
*   **Key Terms:** Independence, Audit Charter, CAATs (Computer-Assisted Audit Techniques).

---

### **D. Final Study Tips**

#### **Top 20 Quiz Points**
1.  **Board** approves high-level **Policies**; **Senior Management** approves **Standards/Procedures**.
2.  **CITO and CISO** roles are mandatory.
3.  Primary **Data Centers** must be in **Cambodia**.
4.  Policies must be reviewed **annually**.
5.  **Penetration Testing** is an **annual** requirement.
6.  **Red Teaming** exercises happen every **two years**.
7.  **2FA** is required for online banking and remote VPN access.
8.  **Cloud usage** for critical systems needs **NBC approval**.
9.  **Outsourcing contracts** must allow **NBC to audit** the vendor.
10. **RTO/RPO** must be defined in the Business Continuity Plan.
11. **Encryption** is required for data at rest and in transit.
12. **RBAC** ensures users only get access they need for their specific job.
13. **Asset Inventories** must include hardware, software, and data.
14. **Audit logs** for critical systems must be kept for **3 years**.
15. **NBC approval** is required before hosting customer data **outside Cambodia**.
16. **AI/ML** systems must be assessed for **ethical use and bias**.
17. **SSTs (ATMs)** require **anti-skimming** and **CCTV**.
18. **Incident reporting** to the NBC is mandatory.
19. **IT Audit Lead** must be certified with **3+ years experience**.
20. **Quantum Security:** BFIs must plan for a move to quantum-resistant encryption.

#### **What to Memorize First**
*   **Approval Levels:** Board (Policy/Strategy) vs. Senior Management (Procedures/Implementation).
*   **Mandatory Roles:** CITO, CISO, IT Audit.
*   **Timeframes:** Annual (Policy review, Pen Test), Every 2 years (Red Team), 3 years (Audit logs, Strategy review).

#### **Common Exam Traps**
*   **Trap:** The Board approves every IT procedure. **Fact:** No, the Board approves *Policies*; Senior Management approves *Procedures*.
*   **Trap:** BFIs can host their main database anywhere. **Fact:** The *primary* data center must be in Cambodia.
*   **Trap:** Vulnerability Scanning and Penetration Testing are the same. **Fact:** Scanning is automated/regular; Pen Testing is an in-depth simulation at least once a year.