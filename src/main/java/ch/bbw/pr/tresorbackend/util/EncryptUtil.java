package ch.bbw.pr.tresorbackend.util;

import javax.crypto.Cipher;
import javax.crypto.spec.GCMParameterSpec;
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
   private String salt;
   private GCMParameterSpec iv;

   public EncryptUtil(String password, String salt, byte[] iv)  {
      try {
         byte[] key = password.getBytes(StandardCharsets.UTF_8);
         MessageDigest sha = MessageDigest.getInstance("SHA-256");
         key = sha.digest(key);
         key = Arrays.copyOf(key, 16);
         this.secretKey = new SecretKeySpec(key, "AES");
      } catch (Exception e) {
         System.out.println("Fehler beim Schlüsselaufbau: " + e.getMessage());
      }
      this.salt = salt;
      this.iv = new GCMParameterSpec(128, iv);
   }

   public String encrypt(String data) {
          Cipher cipher = Cipher.getInstance("AES/GCM/NoPadding");
         cipher.init(Cipher.ENCRYPT_MODE, key, iv);
         byte[] cipherText = cipher.doFinal(input.getBytes());
         return Base64.getEncoder()
                 .encodeToString(cipherText);
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
