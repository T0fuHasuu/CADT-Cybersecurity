public class VulnerableRfidAuth {

    static class User {
        public String username;
        public String password; 
    }

    public static void main(String[] args) {
        System.out.println("=== VULNERABLE RFID AUTH DEMO ===");

        User stored = new User();
        stored.username = "alice";
        stored.password = "da"; 

        String attackerProvidedPassword = ""; 

        if (attackerProvidedPassword.equals(stored.password)) {
            System.out.println("Auth SUCCESS — attacker logged in as " 
            + stored.username);
        } else {
            System.out.println("Auth FAILED");
        }
    }
}
