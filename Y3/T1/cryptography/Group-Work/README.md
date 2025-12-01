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
