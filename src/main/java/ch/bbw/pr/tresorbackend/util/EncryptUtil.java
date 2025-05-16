package ch.bbw.pr.tresorbackend.util;

import javax.crypto.Cipher;
import javax.crypto.SecretKey;
import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.GCMParameterSpec;
import javax.crypto.spec.PBEKeySpec;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.SecureRandom;
import java.security.spec.KeySpec;
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

   private SecretKey secretKey;
   private GCMParameterSpec iv;

   public EncryptUtil(String password, String salt, byte[] iv)  {
      try {
         this.secretKey = getKeyFromPassword(password,salt);
      } catch (Exception e) {
         System.out.println("Fehler beim Schlüsselaufbau: " + e.getMessage());
      }
      this.iv = new GCMParameterSpec(128, iv);
   }

   public String encrypt(String data) {
      try{
          Cipher cipher = Cipher.getInstance("AES/GCM/NoPadding");
         cipher.init(Cipher.ENCRYPT_MODE, this.secretKey, this.iv);
         byte[] cipherText = cipher.doFinal(data.getBytes());
         return Base64.getEncoder()
                 .encodeToString(cipherText);
   } catch(Exception e){
         e.printStackTrace();
      }
      return null;
   }

   public String decrypt(String data) {
      try{
         Cipher cipher = Cipher.getInstance("AES/GCM/NoPadding");
         cipher.init(Cipher.DECRYPT_MODE, this.secretKey, this.iv);

         return new String(cipher.doFinal(Base64.getDecoder().decode(data)));
      } catch(Exception e){
         e.printStackTrace();
      }
      return null;
   }

   public static GCMParameterSpec generateIv() {

      byte[] iv = new byte[12];

      new SecureRandom().nextBytes(iv);

      return new GCMParameterSpec(128, iv);

   }

   public static String generateSalt(int size) {

      SecureRandom random = new SecureRandom();

      byte[] salt = new byte[size];

      random.nextBytes(salt);

      return Base64.getEncoder().encodeToString(salt);

   }

   private static SecretKey getKeyFromPassword(String password, String salt) {
      try {
         SecretKeyFactory factory = SecretKeyFactory.getInstance("PBKDF2WithHmacSHA256");
         KeySpec spec = new PBEKeySpec(password.toCharArray(), salt.getBytes(), 65536, 256);
         SecretKey secret = new SecretKeySpec(factory.generateSecret(spec).getEncoded(), "AES");

         return secret;
      } catch (Exception e) {
         e.printStackTrace();
      }
      return  null;
   }
}
