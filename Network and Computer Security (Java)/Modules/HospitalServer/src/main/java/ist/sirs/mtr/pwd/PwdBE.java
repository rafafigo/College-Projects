package ist.sirs.mtr.pwd;

import ist.sirs.mtr.error.AssertError;

import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.PBEKeySpec;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.security.spec.InvalidKeySpecException;
import java.util.Arrays;

public class PwdBE {

  private static final SecureRandom secRandom = new SecureRandom();
  private static final String KDF_ALGORITHM = "PBKDF2WithHmacSHA512";
  private static final int KDF_ITERATIONS = 500;
  private static final int KDF_KEY_LENGTH = 512;

  public static byte[] newSalt() {
    byte[] salt = new byte[16];
    secRandom.nextBytes(salt);
    return salt;
  }

  public static boolean isExpectedPwd(String pwd, byte[] pwdHash, byte[] salt) {
    return Arrays.equals(hash(pwd, salt), pwdHash);
  }

  public static byte[] hash(String pwd, byte[] salt) {
    try {
      PBEKeySpec spec = new PBEKeySpec(pwd.toCharArray(), salt, KDF_ITERATIONS, KDF_KEY_LENGTH);
      SecretKeyFactory skf = SecretKeyFactory.getInstance(KDF_ALGORITHM);
      return skf.generateSecret(spec).getEncoded();
    } catch (NoSuchAlgorithmException | InvalidKeySpecException e) {
      throw new AssertError(PwdBE.class.getSimpleName(), "hash", e);
    }
  }
}
