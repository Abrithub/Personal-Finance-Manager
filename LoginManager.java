import java.util.HashMap;
import java.util.Map;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.nio.charset.StandardCharsets;

public class LoginManager {
    private static Map<String, User> users = new HashMap<>();
    private static User currentUser;

    public static boolean register(String username, String password, String email, String fullName) {
        if (users.containsKey(username)) {
            return false; // Username already exists
        }

        String hashedPassword = hashPassword(password);
        User newUser = new User(username, hashedPassword, email, fullName);
        users.put(username, newUser);
        return true;
    }

    public static boolean login(String username, String password) {
        User user = users.get(username);
        if (user == null) {
            return false;
        }

        String hashedPassword = hashPassword(password);
        if (user.getPassword().equals(hashedPassword)) {
            currentUser = user;
            return true;
        }
        return false;
    }

    public static void logout() {
        currentUser = null;
    }

    public static User getCurrentUser() {
        return currentUser;
    }

    private static String hashPassword(String password) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] hash = digest.digest(password.getBytes(StandardCharsets.UTF_8));
            StringBuilder hexString = new StringBuilder();
            for (byte b : hash) {
                String hex = Integer.toHexString(0xff & b);
                if (hex.length() == 1) hexString.append('0');
                hexString.append(hex);
            }
            return hexString.toString();
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException(e);
        }
    }
} 