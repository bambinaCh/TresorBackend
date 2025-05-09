package ch.bbw.pr.tresorbackend.util;

import javax.crypto.Cipher;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.util.Arrays;
import java.util.Base64;
import org.jasypt.util.text.AES256TextEncryptor;

/**
 * EncryptUtil
 * Used to encrypt content.
 * @author Chaimaa El Jarite
 * @author Peter Rutschmann
 */
public class EncryptUtil {

   private SecretKeySpec secretKey;

   public EncryptUtil(String password) {
      try {
         byte[] key = password.getBytes(StandardCharsets.UTF_8);
         MessageDigest sha = MessageDigest.getInstance("SHA-256");
         key = sha.digest(key);
         key = Arrays.copyOf(key, 16);
         this.secretKey = new SecretKeySpec(key, "AES");
      } catch (Exception e) {
         System.out.println("Fehler beim Schlüsselaufbau: " + e.getMessage());
      }
   }

   public String encrypt(String data) {
      try {
         Cipher cipher = Cipher.getInstance("AES/ECB/PKCS5Padding");
         cipher.init(Cipher.ENCRYPT_MODE, secretKey);
         byte[] encrypted = cipher.doFinal(data.getBytes(StandardCharsets.UTF_8));
         return Base64.getEncoder().encodeToString(encrypted);
      } catch (Exception e) {
         System.out.println("Fehler bei der Verschluesselung: " + e.getMessage());
         return null;
      }
   }

   public String decrypt(String data) {
      try {
         Cipher cipher = Cipher.getInstance("AES/ECB/PKCS5Padding");
         cipher.init(Cipher.DECRYPT_MODE, secretKey);
         byte[] decrypted = cipher.doFinal(Base64.getDecoder().decode(data));
         return new String(decrypted, StandardCharsets.UTF_8);
      } catch (Exception e) {
         System.out.println("Fehler bei der Entschluesselung: " + e.getMessage());
         return null;
      }
   }
}
