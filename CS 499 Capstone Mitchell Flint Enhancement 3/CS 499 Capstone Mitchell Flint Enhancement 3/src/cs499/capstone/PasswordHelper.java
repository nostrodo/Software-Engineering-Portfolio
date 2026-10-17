package cs499.capstone;
import java.security.SecureRandom;
import java.security.MessageDigest;
import java.security.GeneralSecurityException;
import java.util.Base64;
import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.PBEKeySpec;

public class PasswordHelper {
	
	// PBKDF2 settings
	private static final int ITERATIONS = 600000;
	private static final int KEY_LENGTH = 256;
	
	// generate random salt
	public static String generateSalt() {
		byte[] salt = new byte[16];
		new SecureRandom().nextBytes(salt);
		return Base64.getEncoder().encodeToString(salt);
	}
	
	// creates a password hash
	public static String hashPassword(String password, String salt) throws GeneralSecurityException {
		byte[] saltBytes = Base64.getDecoder().decode(salt);
		PBEKeySpec spec = new PBEKeySpec(password.toCharArray(), saltBytes, ITERATIONS, KEY_LENGTH);
		
		try {
			SecretKeyFactory factory = SecretKeyFactory.getInstance("PBKDF2WithHmacSHA256");
			byte[] hash = factory.generateSecret(spec).getEncoded();
			return Base64.getEncoder().encodeToString(hash);
			
		} finally { spec.clearPassword(); }
	}
	
	// compare the pass with the stored hash
	public static boolean verifyPassword(String password, String salt, String storedHash) throws GeneralSecurityException {
		byte[] enteredHash = Base64.getDecoder().decode(hashPassword(password, salt));
		byte[] expectedHash = Base64.getDecoder().decode(storedHash);
		
		return MessageDigest.isEqual(enteredHash, expectedHash);
	}
}
