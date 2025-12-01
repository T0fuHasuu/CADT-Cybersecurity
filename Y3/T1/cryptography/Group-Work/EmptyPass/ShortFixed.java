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
