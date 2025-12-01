import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.util.Base64;

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
        
        // Modification Attack
        System.out.println("=== VULNERABLE VERSION ===");

        Data v = new Data();

        v.username = attackerData.split("=")[1];

        System.out.println("App believes username = " + v.username);
  
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
        System.out.println("username = " + s.username);
    }
}
