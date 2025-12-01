# **Demonstration**

## **[Hardcode Sensitive Information](./SensitiveInfo/CustomTabsSession2.java) <-- Source**

```java
package o;

import com.aba.ibank.model.responses.BaseResponse;
import java.util.ArrayList;

public final class CustomTabsSession2 extends BaseResponse {
    public static final int $stable = 8;
    @NotificationCompatExtras(ICustomTabsCallback = "l_name")
    public ArrayList<String> suggestions;
    @NotificationCompatExtras(ICustomTabsCallback = "username")
    public String username = "";
}
```

### Error

```java
public ArrayList<String> suggestions;
public String username = "";
```

> **Because the class has public fields and no cryptography, an attacker can send fake data (like fake username, fake suggestions). The app trusts the data because nothing checks if it was changed. So the attacker can modify the data → the app believes it → attack succeeds.**

### **How to Exploit ? [File](./SensitiveInfo/SimpleIntegrityDemo.java) <- Source**

#### **Modification**

```java
public class SimpleIntegrityDemo {

    static class Data {
        public String username;
    }

    public static void main(String[] args) throws Exception {
        
        String originalData = "username=real_user";
        String attackerData = "username=hacked_user";
        
        // Modification Attack
        System.out.println("=== VULNERABLE VERSION ===");

        Data v = new Data();

        v.username = attackerData.split("=")[1];

        System.out.println("App believes username = " + v.username);}}
```

**Code Explaination**

- `static class Data {public String username;}` : Construct A Class
- `String originalData = "username=real_user";` : Declare Variable
- `Data v = new Data();` : Create Object
- `v.username = attackerData.split("=")[1];` : Insert Data Into Object

**Output**

```bash
=== VULNERABLE VERSION ===
App believes username = hacked_user
```

**Summary :**

- App accepts whatever data arrives.
- attacker changes "real_user" → "hacked_user"
- App trusts it → attack succeeds

---

#### **Solution**

```java
public class SimpleIntegrityDemo {

    static class Data {
        public String username;
    }

    static String hmac(String data, String key) throws Exception {
        Mac mac = Mac.getInstance("HmacSHA256");
        mac.init(new SecretKeySpec(key.getBytes(), "HmacSHA256"));
        return Base64.getEncoder().encodeToString(mac.doFinal(data.getBytes()));
    }

    public static void main(String[] args) throws Exception {
        
        String originalData = "username=real_user";
        String attackerData = "username=hacked_user";

        // Fixing
        System.out.println("\n=== FIXED (CRYPTO-PROTECTED) VERSION ===");
 
        String key = "mysecretkey123"; 

        String signature = hmac(originalData, key);

        if (!hmac(attackerData, key).equals(signature)) {
            System.out.println("Tampering detected! Rejecting data.");
            return; 
        }

        Data s = new Data();
        s.username = attackerData.split("=")[1];
        System.out.println("username = " + s.username);}}
```

**Code Explaination**

- HMAC Function To Encrypt And Generate A Signature

    ```java
    static String hmac(String data, String key) throws Exception {
    Mac mac = Mac.getInstance("HmacSHA256");
    mac.init(new SecretKeySpec(key.getBytes(), "HmacSHA256"));
    return Base64.getEncoder().encodeToString(mac.doFinal(data.getBytes()));}
    ```

- `String key = "mysecretkey123";` : Initiate a key for signature
- `String signature = hmac(originalData, key);` : Encrypt and Generate Signature
- Check if the Signature the same

    ```java
    if (!hmac(attackerData, key).equals(signature)) {
        System.out.println("Tampering detected! Rejecting data.");
        return; }
    ```

- `Data s = new Data();` : Create Object
- `s.username = attackerData.split("=")[1];` : Insert Data Into Object which can't due to tempered

**Output**

```bash
=== FIXED (CRYPTO-PROTECTED) VERSION ===
Tampering detected! Rejecting data.
```

**Summary :**

- Server puts an HMAC "stamp" on the data
- App checks HMAC before accepting it
- attacker changes data → stamp is wrong → attack blocked

## **[Empty String Password](./EmptyPass/RfidScenario.java) <-- Source**

```java
public final class RfidScenario {
    public String rfidId = "";
    public String password = ""; 
}
```

### Error

```bash
public String password = "";
```

> **Problem:** password field is empty or left as default. If auth logic accepts empty or treats empty as valid, attacker can bypass authentication by supplying an empty password (or via other input vectors). Storing or accepting empty/plaintext passwords is insecure.

### **How to Exploit?**

#### **[Attack](./EmptyPass/VulnerableRfidAuth.java) <- Source**

```java
public class VulnerableRfidAuth {

    static class User {
        public String username;
        public String password; 
    }

    public static void main(String[] args) {
        System.out.println("=== VULNERABLE RFID AUTH DEMO ===");

        User stored = new User();
        stored.username = "alice";
        stored.password = ""; 

        String attackerProvidedPassword = ""; 

        if (attackerProvidedPassword.equals(stored.password)) {
            System.out.println("Auth SUCCESS — attacker logged in as " + stored.username);
        } else {
            System.out.println("Auth FAILED");}}}
```

**Code Explanation**

- `static class User { public String password; }` : Construct a class representing a user (holds password).
- `stored.password = "";` : Store an **empty** password (bad).
- `String attackerProvidedPassword = "";` : Attacker supplies empty password.
- `if (attackerProvidedPassword.equals(stored.password))` : Naive check — attacker wins.

**Output**

```bash
=== VULNERABLE RFID AUTH DEMO ===
Auth SUCCESS ? attacker logged in as alice
```

**Summary**

- App stored or accepted an empty password.
- Attacker supplies empty password → login succeeds.
- Root cause: empty/hardcoded plaintext password and no validation or secure storage.


#### **[Solution](./EmptyPass/ShortFixed.java) <- Source**

```java
import java.security.MessageDigest;
import java.security.SecureRandom;
import java.util.Base64;

public class ShortFixed {
    static String hash(byte[] salt, String pwd) throws Exception {
        MessageDigest md = MessageDigest.getInstance("SHA-256");
        md.update(salt);
        md.update(pwd.getBytes("UTF-8"));
        return Base64.getEncoder().encodeToString(md.digest());
    }

    static boolean ctEq(byte[] a, byte[] b) { 
        if (a.length != b.length) return false;
        int d = 0;
        for (int i = 0; i < a.length; i++) d |= a[i] ^ b[i];
        return d == 0;
    }

    public static void main(String[] args) throws Exception {
    
        String plain = "S3cureP@ss";
        byte[] salt = new byte[12]; new SecureRandom().nextBytes(salt);
        String storedHash = hash(salt, plain);


        String attempt = "S3cureP@sss"; 

        if (attempt == null || attempt.isEmpty()) {                 
            System.out.println("Rejected: empty password not allowed.");
            return;
        }

        String attemptHash = hash(salt, attempt);
        if (ctEq(attemptHash.getBytes("UTF-8"), storedHash.getBytes("UTF-8")))
            System.out.println("Auth SUCCESS");
        else
            System.out.println("Auth FAILED");
    }
}
```

- Hash Function To Protect Password

```java
static String hash(byte[] salt, String pwd) throws Exception {
    MessageDigest md = MessageDigest.getInstance("SHA-256");
    md.update(salt);
    md.update(pwd.getBytes("UTF-8"));
    return Base64.getEncoder().encodeToString(md.digest());
}
```

- Generate Salt + Hash the Real Password

```java
byte[] salt = new byte[12];
new SecureRandom().nextBytes(salt);
String storedHash = hash(salt, realPassword);
```

- Reject Empty Password

```java
if (attempt == null || attempt.isEmpty()) {
    System.out.println("Rejected: empty password not allowed.");
    return;
}
```

- Validate Password Using Constant-Time Comparison

```java
String attemptHash = hash(salt, attempt);
if (ctEq(attemptHash.getBytes("UTF-8"), storedHash.getBytes("UTF-8")))
```

**Output**

```bash
=== FIXED (CRYPTO-PROTECTED PASSWORD) ===
Rejected: empty password not allowed.
```

**Summary**

- Empty passwords are immediately blocked
- Passwords are **never stored in plain text**
- Uses **Salt + SHA-256** to secure them
- Uses **constant-time compare** to avoid timing attacks
- Attacker cannot bypass login with an empty or modified password
