package dental.core.users.util;

import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

public class PasswordHashGenerator {
    public static void main(String[] args) {
        BCryptPasswordEncoder encoder = new BCryptPasswordEncoder();
        String password = "123456";
        
        // Generar hash para "123456"
        String hash = encoder.encode(password);
        System.out.println("Password: " + password);
        System.out.println("BCrypt Hash: " + hash);
        
        // Verificar que funciona
        boolean matches = encoder.matches(password, hash);
        System.out.println("Verification: " + matches);
        
        // Generar algunos hashes más para tener opciones
        System.out.println("\n=== Additional hashes ===");
        for (int i = 0; i < 3; i++) {
            String newHash = encoder.encode(password);
            System.out.println("Hash " + (i + 1) + ": " + newHash);
            System.out.println("Verification: " + encoder.matches(password, newHash));
        }
    }
}

