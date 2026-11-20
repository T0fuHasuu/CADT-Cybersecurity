So this is one of my self project which I did during the course of cryptography in the major of cybersecurity. The previous work was somewhat unsatified which that time I had trouble with the esp32 and also the computer which lead to rush finishes. Looking back I can see many flaw and impractical usage in the modern world or in general. The purpose is to have it like people can carry it as a key to everywhere they go even with public device, it can still securely do its job as password management. Firebase is fine but it seems just not really what I want but well if it work it works. I also want to make it secure, not just code wise but process wise too, I was planning on integrating it with telegram bot or something that make easier for user to get access to their credential and such with security. I want to finish innovate it so that I can put it inside my cv that doesn't feel like a "trash project", I just keep getting busy and didn't have time to think much of but now that I have a few time to spare, I want to atleast leave it more a good concept prototype or just handful for other people that's all. For now anaylze and understand the core functions and usage of this via the code and documents which attached below. Lemme know if there're part which bother you or suggestion and reocmmmendation for better improvement and such. Attachment below :


Code : "#include <WiFi.h>
#include <HTTPClient.h>
#include <WiFiClientSecure.h>
#include <ArduinoJson.h>
#include <mbedtls/gcm.h>
#include <mbedtls/base64.h>
#include <mbedtls/sha256.h>
#include <esp_system.h> // esp_fill_random

// ====== CONFIG - EDIT BEFORE USE ======
const char *WIFI_SSID = "SSID";
const char *WIFI_PASS = "PASSWORD";

const char *FIREBASE_HOST = "your-project-id.firebaseio.com"; // no https://
const char *FIREBASE_AUTH = "";                               // your DB secret or token (optional)

// ====== SETTINGS ======
#define MASTER_SECRET "YUTH" // string whose SHA256 is used as login check
#define MAX_SERIAL_READ 256

// ====== HELPERS: Base64 ======
String base64_encode_buf(const uint8_t *data, size_t len)
{
  size_t out_len = 4 * ((len + 2) / 3) + 1;
  unsigned char *out = (unsigned char *)malloc(out_len);
  if (!out)
    return String();
  size_t olen = 0;
  int rc = mbedtls_base64_encode(out, out_len, &olen, data, len);
  String s;
  if (rc == 0)
    s = String((char *)out, olen);
  free(out);
  return s;
}

bool base64_decode_to_buf(const String &in, uint8_t *out_buf, size_t &out_len)
{
  size_t in_len = in.length();
  size_t needed = (in_len / 4) * 3 + 4;
  unsigned char *tmp = (unsigned char *)malloc(needed);
  if (!tmp)
    return false;
  int rc = mbedtls_base64_decode(tmp, needed, &out_len, (const unsigned char *)in.c_str(), in_len);
  if (rc == 0)
  {
    memcpy(out_buf, tmp, out_len);
    free(tmp);
    return true;
  }
  else
  {
    free(tmp);
    return false;
  }
}

// ====== HELPERS: Random bytes ======
void random_bytes(uint8_t *buf, size_t len)
{
  esp_fill_random(buf, len);
}

// ====== AES-GCM wrappers (mbedTLS) ======
// key_len bytes (32), iv_len should be 12, tag_len typically 16
bool aes256_gcm_encrypt(const uint8_t *key, size_t key_len,
                        const uint8_t *iv, size_t iv_len,
                        const uint8_t *plain, size_t plain_len,
                        uint8_t *out_cipher, uint8_t *out_tag, size_t tag_len)
{
  if (key_len != 32 || iv_len != 12 || tag_len < 12)
    return false;
  mbedtls_gcm_context gcm;
  mbedtls_gcm_init(&gcm);
  if (mbedtls_gcm_setkey(&gcm, MBEDTLS_CIPHER_ID_AES, key, key_len * 8) != 0)
  {
    mbedtls_gcm_free(&gcm);
    return false;
  }
  int rc = mbedtls_gcm_crypt_and_tag(&gcm,
                                     MBEDTLS_GCM_ENCRYPT,
                                     plain_len,
                                     iv, iv_len,
                                     NULL, 0,
                                     plain, out_cipher,
                                     tag_len, out_tag);
  mbedtls_gcm_free(&gcm);
  return (rc == 0);
}

bool aes256_gcm_decrypt(const uint8_t *key, size_t key_len,
                        const uint8_t *iv, size_t iv_len,
                        const uint8_t *cipher, size_t cipher_len,
                        const uint8_t *tag, size_t tag_len,
                        uint8_t *out_plain)
{
  if (key_len != 32 || iv_len != 12 || tag_len < 12)
    return false;
  mbedtls_gcm_context gcm;
  mbedtls_gcm_init(&gcm);
  if (mbedtls_gcm_setkey(&gcm, MBEDTLS_CIPHER_ID_AES, key, key_len * 8) != 0)
  {
    mbedtls_gcm_free(&gcm);
    return false;
  }
  int rc = mbedtls_gcm_auth_decrypt(&gcm,
                                    cipher_len,
                                    iv, iv_len,
                                    NULL, 0,
                                    tag, tag_len,
                                    cipher, out_plain);
  mbedtls_gcm_free(&gcm);
  return (rc == 0);
}

// ====== SHA-256 helpers ======
void sha256_bytes(const uint8_t *data, size_t len, uint8_t out32[32])
{
  mbedtls_sha256_ret(data, len, out32, 0);
}

String sha256_hex_string(const char *s)
{
  uint8_t out[32];
  sha256_bytes((const uint8_t *)s, strlen(s), out);
  // hex
  char buf[65];
  buf[64] = 0;
  for (int i = 0; i < 32; i++)
    sprintf(buf + i * 2, "%02x", out[i]);
  return String(buf);
}

bool sha256_equals_input(const char *input, const uint8_t expected_hash[32])
{
  uint8_t out[32];
  sha256_bytes((const uint8_t *)input, strlen(input), out);
  return (memcmp(out, expected_hash, 32) == 0);
}

// ====== Firebase REST helpers (simple) ======
String firebase_put(const String &path, const String &json_payload)
{
  WiFiClientSecure client;
  client.setInsecure(); // dev only - replace for production
  HTTPClient https;
  String url = String("https://") + FIREBASE_HOST + path + ".json";
  if (strlen(FIREBASE_AUTH) > 0)
    url += String("?auth=") + FIREBASE_AUTH;
  https.begin(client, url);
  https.addHeader("Content-Type", "application/json");
  int code = https.PUT(json_payload);
  String resp = https.getString();
  https.end();
  if (code >= 200 && code < 300)
    return resp;
  return String(); // empty means fail
}

String firebase_get(const String &path)
{
  WiFiClientSecure client;
  client.setInsecure();
  HTTPClient https;
  String url = String("https://") + FIREBASE_HOST + path + ".json";
  if (strlen(FIREBASE_AUTH) > 0)
    url += String("?auth=") + FIREBASE_AUTH;
  https.begin(client, url);
  int code = https.GET();
  String resp = https.getString();
  https.end();
  if (code >= 200 && code < 300)
    return resp;
  return String();
}

// ====== Serial helper (read line) ======
String serialReadLine()
{
  String s;
  while (true)
  {
    while (Serial.available() == 0)
    {
      delay(10);
    }
    char c = Serial.read();
    if (c == '\r')
      continue;
    if (c == '\n')
      break;
    s += c;
    if (s.length() >= MAX_SERIAL_READ - 1)
      break;
  }
  s.trim();
  return s;
}

// ====== Program flows ======
uint8_t MASTER_HASH[32];

bool promptMasterPassphrase()
{
  int attempts = 0;
  while (attempts < 3)
  {
    Serial.print("Enter passphrase to access: ");
    String input = serialReadLine();
    if (sha256_equals_input(input.c_str(), MASTER_HASH))
    {
      Serial.println("Auth OK.");
      return true;
    }
    else
    {
      attempts++;
      Serial.printf("Wrong pass (%d/3)\n", attempts);
    }
  }
  Serial.println("Too many failed attempts. Timeout 60s.");
  delay(60000);
  return false;
}

void createPasswordFlow()
{
  Serial.println("-- CREATE NEW PASSWORD --");
  Serial.print("Enter name (identifier): ");
  String name = serialReadLine();
  if (name.length() == 0)
  {
    Serial.println("Empty name. Aborting.");
    return;
  }

  Serial.print("Enter plaintext password to store: ");
  String pwd = serialReadLine();
  size_t plain_len = pwd.length();
  if (plain_len == 0)
  {
    Serial.println("Empty password. Aborting.");
    return;
  }

  // generate key + iv
  uint8_t key[32];
  random_bytes(key, sizeof(key));
  uint8_t iv[12];
  random_bytes(iv, sizeof(iv));
  uint8_t *cipher = (uint8_t *)malloc(plain_len);
  uint8_t tag[16];

  bool ok = aes256_gcm_encrypt(key, sizeof(key), iv, sizeof(iv),
                               (const uint8_t *)pwd.c_str(), plain_len,
                               cipher, tag, sizeof(tag));
  if (!ok)
  {
    Serial.println("Encryption failed.");
    free(cipher);
    return;
  }

  // base64 encode fields
  String b64_cipher = base64_encode_buf(cipher, plain_len);
  String b64_iv = base64_encode_buf(iv, sizeof(iv));
  String b64_tag = base64_encode_buf(tag, sizeof(tag));
  String b64_key = base64_encode_buf(key, sizeof(key));

  // build JSON for ciphertext
  StaticJsonDocument<512> doc;
  doc["ciphertext"] = b64_cipher;
  doc["iv"] = b64_iv;
  doc["tag"] = b64_tag;
  String jsonCipher;
  serializeJson(doc, jsonCipher);

  // upload ciphertext
  String pathCipher = String("/ciphertexts/") + name;
  String res1 = firebase_put(pathCipher, jsonCipher);
  if (res1.length() == 0)
  {
    Serial.println("Failed to upload ciphertext to Firebase.");
    free(cipher);
    return;
  }

  // store key under /keys/<name>
  StaticJsonDocument<256> dock;
  dock["key"] = b64_key;
  String jsonKey;
  serializeJson(dock, jsonKey);
  String pathKey = String("/keys/") + name;
  String res2 = firebase_put(pathKey, jsonKey);
  if (res2.length() == 0)
  {
    Serial.println("Failed to upload key to Firebase.");
    // Consider deleting ciphertext in real app - omitted for brevity
    free(cipher);
    return;
  }

  Serial.println("Create success.");
  Serial.printf("Uploaded ciphertext path: %s\n", pathCipher.c_str());
  Serial.printf("Stored key path: %s\n", pathKey.c_str());

  // zero sensitive memory
  memset(key, 0, sizeof(key));
  memset(iv, 0, sizeof(iv));
  memset(tag, 0, sizeof(tag));
  free(cipher);
}

void getPasswordFlow()
{
  Serial.println("-- GET / DECRYPT --");
  Serial.print("Enter name (identifier): ");
  String name = serialReadLine();
  if (name.length() == 0)
  {
    Serial.println("Empty name. Aborting.");
    return;
  }

  // Before fetching, require master passphrase again (as requested)
  Serial.println("Enter passphrase for decryption:");
  String pass = serialReadLine();
  if (!sha256_equals_input(pass.c_str(), MASTER_HASH))
  {
    Serial.println("Authentication failed. Aborting.");
    return;
  }

  // fetch ciphertext JSON
  String pathCipher = String("/ciphertexts/") + name;
  String resCipher = firebase_get(pathCipher);
  if (resCipher.length() == 0)
  {
    Serial.println("Failed to fetch ciphertext or entry not found.");
    return;
  }

  // parse JSON
  StaticJsonDocument<1024> doc;
  DeserializationError err = deserializeJson(doc, resCipher);
  if (err)
  {
    Serial.println("Invalid JSON fetched for ciphertext.");
    return;
  }
  String b64_cipher = doc["ciphertext"] | "";
  String b64_iv = doc["iv"] | "";
  String b64_tag = doc["tag"] | "";
  if (b64_cipher == "" || b64_iv == "" || b64_tag == "")
  {
    Serial.println("Missing fields in ciphertext record.");
    return;
  }

  // fetch key JSON
  String pathKey = String("/keys/") + name;
  String resKey = firebase_get(pathKey);
  if (resKey.length() == 0)
  {
    Serial.println("Failed to fetch key or key entry not found.");
    return;
  }
  StaticJsonDocument<512> dock;
  DeserializationError err2 = deserializeJson(dock, resKey);
  if (err2)
  {
    Serial.println("Invalid JSON fetched for key.");
    return;
  }
  String b64_key = dock["key"] | "";
  if (b64_key == "")
  {
    Serial.println("Missing key field in key record.");
    return;
  }

  // decode base64
  size_t cipher_max = (b64_cipher.length() / 4) * 3 + 4;
  uint8_t *cipher = (uint8_t *)malloc(cipher_max);
  size_t cipher_len = 0;
  if (!base64_decode_to_buf(b64_cipher, cipher, cipher_len))
  {
    free(cipher);
    Serial.println("Bad base64 cipher");
    return;
  }

  uint8_t iv[12];
  size_t iv_len = 0;
  if (!base64_decode_to_buf(b64_iv, iv, iv_len) || iv_len != 12)
  {
    free(cipher);
    Serial.println("Bad IV");
    return;
  }

  uint8_t tag[32];
  size_t tag_len = 0;
  if (!base64_decode_to_buf(b64_tag, tag, tag_len) || tag_len < 12)
  {
    free(cipher);
    Serial.println("Bad tag");
    return;
  }

  uint8_t key[32];
  size_t key_len = 0;
  if (!base64_decode_to_buf(b64_key, key, key_len) || key_len != 32)
  {
    free(cipher);
    Serial.println("Bad key stored");
    return;
  }

  // decrypt
  uint8_t *plain = (uint8_t *)malloc(cipher_len + 1);
  bool ok = aes256_gcm_decrypt(key, sizeof(key), iv, iv_len, cipher, cipher_len, tag, tag_len, plain);
  if (!ok)
  {
    Serial.println("Decryption failed (auth tag mismatch or wrong key).");
  }
  else
  {
    plain[cipher_len] = 0;
    Serial.printf("Decrypted plaintext for '%s': %s\n", name.c_str(), (char *)plain);
  }

  // cleanup & zero sensitive memory
  memset(key, 0, sizeof(key));
  memset(iv, 0, sizeof(iv));
  free(cipher);
  free(plain);
}

// ====== Setup and main loop ======
void connectWiFi()
{
  Serial.printf("Connecting to WiFi '%s' ...\n", WIFI_SSID);
  WiFi.mode(WIFI_STA);
  WiFi.begin(WIFI_SSID, WIFI_PASS);

  int cnt = 0;
  while (WiFi.status() != WL_CONNECTED)
  {
    delay(300);
    Serial.print(".");
    if (++cnt > 200)
    {
      Serial.println("\nWiFi connect timeout. Restarting.");
      ESP.restart();
    }
  }
  Serial.println("\nWiFi connected. IP: " + WiFi.localIP().toString());
}

void setup()
{
  Serial.begin(115200);
  delay(200);
  connectWiFi();

  // compute MASTER_HASH = SHA256("YUTH")
  sha256_bytes((const uint8_t *)MASTER_SECRET, strlen(MASTER_SECRET), MASTER_HASH);

  Serial.println("\n--- ESP32 AES-GCM Password Vault (Serial UI) ---");
  // initial auth
  if (!promptMasterPassphrase())
  {
    Serial.println("Initial auth failed. Rebooting...");
    ESP.restart();
  }

  // main menu loop - handled in loop()
}

void loop()
{
  // menu
  Serial.println("\nMain Menu:");
  Serial.println("1) Create Password");
  Serial.println("2) Get Password");
  Serial.println("3) Quit / Restart");
  Serial.print("Choose option [1-3]: ");
  String opt = serialReadLine();

  if (opt == "1")
  {
    createPasswordFlow();
  }
  else if (opt == "2")
  {
    getPasswordFlow();
  }
  else if (opt == "3")
  {
    Serial.println("Quitting. Restarting device.");
    delay(1000);
    ESP.restart();
  }
  else
  {
    Serial.println("Invalid option.");
  }

  delay(200);
}

"
















README.md : "# **ESP32-Based Password Manager with Salted SHA-256 and Firebase Storage**

## **Instruction**

![instruction](./IMG/self-project-instruction.jpg)

---

## **Project Overview**

This project implements a **hardware-assisted password management system** using an **ESP32 microcontroller**, **SHA-256 cryptographic hashing**, and **Firebase Realtime Database** as a remote storage backend.

The ESP32 acts as a **trusted cryptographic processor** that generates secure random salts, computes salted SHA-256 password hashes, and verifies user-supplied passwords through a built-in **HTTP web interface**.

All passwords are protected using **salted hashing**, and **no plaintext passwords are stored** on the device or in Firebase.

This project demonstrates applied knowledge in **embedded cryptography**, **secure password storage**, **authentication mechanisms**, and **IoT–cloud integration**, designed for academic and learning purposes.

---

# **Objectives**

1. Provide a secure method to store and verify passwords without storing plaintext credentials.
2. Use the ESP32 as a trusted cryptographic core for hashing and verification.
3. Implement salted SHA-256 hashing using a hardware-based random salt.
4. Protect verification with an additional SHA-256–based authorization step.
5. Integrate embedded hardware, Wi-Fi, Firebase, and cryptographic libraries into a functional security application.

---

# **System Architecture (High-Level)**

### **1. ESP32 (Trusted Cryptographic Core)**

- Generates cryptographically secure random salts
- Computes SHA-256 hashes using mbedTLS
- Verifies passwords locally
- Hosts a lightweight HTTP web server
- Connects to Wi-Fi automatically on boot
- Performs all cryptographic operations locally

### **2. Firebase Realtime Database (Untrusted Storage)**

Firebase **never stores plaintext passwords**.

Stored data consists only of salts and hashes.

| Path                         | Contents                  |
| ---------------------------- | ------------------------- |
| **`/passwords/<name>/salt`** | Random salt (hex-encoded) |
| **`/passwords/<name>/hash`** | SHA-256(salt || password) |

This ensures that even if Firebase is compromised, passwords cannot be recovered directly.

### **3. User Web Interface (Browser)**

The user interacts through a browser on the same network:

- Create password entries
- Verify stored passwords
- Trigger ESP32 reboot

No client-side cryptography is performed.

---

# **Directory Summary**

```
SELF-PROJECT/
│
├── main.ino                 
│
├── docs/
│   └── THY_DAYUTH_ESP32_PASSWORD_MANAGER.pdf   
│
├── IMG/
│   └── self-project-instruction.jpg
│   
├── data/
│   └── sample.json
│   └── structure.json
│
├── README.md
│
└── .gitignore
```

---

# **Complete Workflow**

## **1. Initialization**

- ESP32 boots
- Automatically connects to Wi-Fi using configured SSID and password
- Initializes Firebase connection
- Starts an HTTP web server on port 80
- Turns on LED indicator when Wi-Fi is connected

---

## **2. Main Menu (Web Interface)**

The ESP32 provides a simple web UI with the following options:

```
- Create Password
- Verify Password
- Quit (Reboot)
```

---

## **3. Creating a Password (Hashing Path)**

When the user selects **Create Password**:

1. User enters:

   * Password name (identifier)
   * Plaintext password
2. ESP32 generates:

   * Random salt (16 bytes) using `esp_fill_random`
3. ESP32 computes:

   * `SHA256(salt || password)`
4. Firebase stores:

   * **/passwords/name/salt**
   * **/passwords/name/hash**
5. ESP32 confirms successful storage

**Plaintext passwords are never uploaded or stored.**

---

## **4. Verifying a Password (Verification Path)**

When the user selects **Verify Password**:

1. User enters:

   * Password name
   * Authorization hash (SHA-256 gatekeeper)
   * Password to verify
2. ESP32 retrieves:

   * Salt from Firebase
   * Stored hash from Firebase
3. ESP32 computes:

   * `SHA256(salt || input_password)`
4. ESP32 compares:

   * Computed hash vs stored hash
5. Result:

   * **Password VERIFIED**
   * **Password INVALID**

---

## **5. Quit**

- ESP32 reboots
- Clears runtime state
- Requires fresh authentication on next session

---

# **Security Features**

### **1. Salted SHA-256 Hashing**

- Prevents rainbow-table attacks
- Each password has a unique random salt
- Hashing performed using mbedTLS

### **2. Hardware-Based Randomness**

- Uses ESP32 hardware RNG (`esp_fill_random`)
- Ensures unpredictable salt values

### **3. Authorization Gatekeeper**

- Verification requires an additional SHA-256 authorization hash
- Prevents unauthorized verification attempts

### **4. No Plaintext Storage**

- No plaintext passwords stored on ESP32
- No plaintext passwords stored in Firebase

### **5. Local Cryptographic Operations**

- ESP32 performs all hashing and comparisons
- Firebase acts only as untrusted storage

---

# **Installation & Setup**

- ESP32 development board
- Arduino IDE or PlatformIO
- Firebase Realtime Database project
- Wi-Fi network

## **Configuration**

Edit the following values in the sketch before uploading:

```cpp
#define SSID      "YOUR_WIFI"
#define PASSWORD  "YOUR_WIFI_PASSWORD"
#define API_KEY   "YOUR_FIREBASE_API_KEY"
#define DB_URL    "https://your-project-id.firebaseio.com/"
```

# **Running the Program**

1. Upload the sketch to the ESP32
2. Open Serial Monitor (115200 baud)
3. Note the assigned IP address
4. Open a browser and visit:

```
http://<ESP32-IP>/
```

# **References**

- ESP32 Technical Reference Manual
- mbedTLS Documentation
- Firebase Realtime Database Documentation
- NIST SP 800-63B (Password Security)
- William Stallings, *Cryptography and Network Security*


"


Guideline or Document : "


                     
Automatic Zoom
                     
Actual Size
                     
Page Fit
                     
Page Width
                                          
50%
                     
75%
                     
100%
                     
125%
                     
150%
                     
200%
                     
300%
                     
400%
                   

      
 

 

      
Department of Telecommunication and Networking 
 

                          
Specialized in cybersecurity 
 

                                   
                     
 

ESP32 Password Manager
 

 

 

        
Course : Cryptography 
 

        
Year3 | Term 1 
 

        
Instructed by : Mr Meas Sothearath 
 

        
Date of submission : 20/12/2025
 

 

 

 

        
Submitted by : 
THY DAYUTH
 

Table of Contents
 

1. Introduction
 

   
 
1.1 Overview of the Project
 

   
 
1.2 Problem Statement
 

   
 
1.3 Motivation
 

   
 
1.4 Related Cryptographic Concepts
 

2. System Architecture
 

   
 
2.1 Overall Workflow
 

   
 
2.2 Key (Salt) Generation Process
 

   
 
2.3 Password Hashing & Storage Process
 

   
 
2.4 Password Verification Process
 

   
 
2.5 System Flowcharts & Diagrams
 

3. Implementation Details
 

   
 
3.1 Technologies and Libraries Used
 

   
 
3.2 Project Structure
 

   
 
3.3 Key Functions (from ESP32 sketch)
 

   
 
3.4 Web UI Implementation
 

   
 
3.5 Security Practices Followed
 

4. Usage Guide
 

   
 
4.1 Installation & Setup
 

   
 
4.2 Running the Program
 

   
 
4.3 Creating (Storing) Password Entries
 

   
 
4.4 Verifying Passwords
 

   
 
4.5 Expected Outputs & Examples
 

5. Conclusion & Future Work
 

   
 
5.1 Summary
 

   
 
5.2 Future work
 

6. References
 

1. Introduction
 

1.1 
Overview
 
of the Project
 

This project implements an 
ESP32
-
based password manager
 
that stores only 

salts and salted SHA
-
256 password hashes in Firebase Realtime Database. The 

ESP32 provides a small web UI (served on port 80) for creating named password 

entries and verifying them. The device uses the ESP32 hardware RNG for salt 

generati
on and mbedTLS SHA
-
256 for hashing.
 

1.2 Problem Statement
 

Storing plaintext passwords on embedded devices or in cloud backends is 

insecure. The project demonstrates a secure pattern: generate a per
-
entry 

random salt, compute 
SHA256(salt || password)
, and store only the salt and 

resulting hash. Verification recomputes the hash from user
-
supplied password 

and stored salt.
 

1.3 Motivation
 

•
 
Teach secure handling of credentials on constrained devices (ESP32).
 

•
 
Demonstrate use of cryptographic primitives (secure RNG, hashing) and 

cloud integration (Firebase).
 

•
 
Provide a minimal, reproducible embedded example for learning secure 

storage and verification processes.
 
"