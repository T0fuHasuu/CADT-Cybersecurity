# **ESP32-Based Encrypted Password Vault with Firebase Storage**

## **Instruction**

![instruction](./IMG/self-project-instruction.jpg)

## **Project Overview**

This project implements a **hardware-assisted password vault** using an **ESP32 microcontroller**, **AES-256 encryption**, and **Firebase Realtime Database** as a secure remote storage backend.
The ESP32 acts as a standalone cryptographic processor capable of generating, encrypting, decrypting, and retrieving passwords through a terminal interface (PuTTY).

All sensitive data stored in Firebase is **cryptographically protected**, and no plaintext credentials ever leave the ESP32.

This project demonstrates applied skills in secure system design, embedded cryptography, authentication, and integration with cloud services.

# **Objectives**

1. Provide a secure method to store and retrieve passwords remotely without exposing plaintext credentials.
2. Use ESP32 as a trusted cryptographic core for caculations.
3. Implement AES-256-GCM encryption with IV + Tag + Ciphertext separation.
4. Protect decryption with an additional SHA-256–based authentication step.
5. Integrate embedded hardware, Wi-Fi, Firebase, and cryptographic libraries into a functioning security tool.

# **System Architecture (High-Level)**

### **1. ESP32 (Trusted Cryptographic Core)**

* Performs AES-256 encryption and decryption
* Generates IV, Tag, Key, Ciphertext
* Handles SHA-256 user authentication
* Connects to Wi-Fi automatically on boot
* Provides a text-based UI through PuTTY/Serial

### **2. Firebase Realtime Database (Untrusted Storage)**

Data stored here is **never** in plaintext.

Two separate database paths are used:

| Path          | Contents                                                  |
| ------------- | --------------------------------------------------------- |
| **`cipher/`** | Stores encrypted password bundles (IV + Tag + Ciphertext) |
| **`keys/`**   | Stores the AES key used for that entry                    |

Storing the key separately avoids a single-point compromise.

### **3. User Terminal (PuTTY)**

The user interacts via:

* Password prompt
* Menu selection
* Name-based password lookup
* Final authentication (SHA-256 check)

# **Directory Summary**

```
Self-Project/
│
├── src/
│   ├── main.ino       
│
├── data/
│   ├── sample_encrypted.json
│   ├── sample_key.json
│
├── docs/
│   ├── report.pdf
│
├── IMG/
│   ├── self-project-instruction.jpg   
│
├── README.md
│
└── .gitignore
```

# **Complete Workflow**

## **1. Initialization**

* ESP32 boots
* Auto-connects to hotspot using **static Wi-Fi SSID + password**
* Displays a login prompt in PuTTY
* User must enter the correct **access password**

If the user enters the wrong password **3 times**, the system:

* Locks itself
* Enforces a 60-second timeout

## **2. Main Menu**

After successful authentication, the ESP32 displays:

```
1 — Get Password  
2 — Create Password  
3 — Quit
```

## **3. Creating a Password (Encryption Path)**

When the user selects **Create Password**:

1. ESP32 prompts for:

   * The plaintext password
   * A “name” or category (ex: facebook, github, banking)
2. ESP32 generates:

   * AES-256 key (32 bytes)
   * IV (12 bytes)
   * Ciphertext
   * Authentication Tag (GCM output)
3. Firebase receives:

   * **cipher/name → { iv, tag, ciphertext }**
   * **keys/name → { key }**
4. ESP32 confirms success and returns to menu.

**At no point is plaintext uploaded to Firebase.**

## **4. Retrieving a Password (Decryption Path)**

When the user selects **Get Password**:

1. ESP32 asks for the password name
2. ESP32 fetches:

   * Encrypted data from `cipher/name`
   * AES key from `keys/name`
3. Before decryption, ESP32 asks for:

   * SHA-256 authentication: user must enter the hash of a secret string
4. ESP32 locally compares:

   * `hash(input)` vs `prestored_hash`
5. If correct → decrypt
6. ESP32 prints the plaintext password
7. If incorrect → denies access

## **5. Quit**

Terminates the session, closes the menu, and resets the device state.

---

# **Security Features**

### **1. AES-256-GCM Encryption**

* Industry-grade strong encryption
* Provides both confidentiality and integrity
* Outputs: ciphertext, IV, and authentication tag

### **2. Separated Storage (Ciphertext vs Key)**

* Keys and encrypted files stored in different Firebase paths
* Prevents bulk database compromise

### **3. SHA-256 Gatekeeper**

* User must enter a SHA-256 hashed secret to unlock decryption
* Adds an additional authentication layer

### **4. Brute-Force Protection**

* Lockout after 3 wrong login attempts
* 1-minute cooldown delay

### **5. Local-Only Cryptography**

* ESP32 performs all crypto operations
* Firebase holds only encrypted artifacts

