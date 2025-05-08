package ch.bbw.pr.tresorbackend.service;

import org.springframework.stereotype.Service;
import org.springframework.security.crypto.bcrypt.BCrypt;

/**
 * PasswordEncryptionService
 * @author Peter Rutschmann
 * @author Chaimaa El Jarite
 */
@Service
public class PasswordEncryptionService {

   private static final String PEPPER = "g3h31m3rP3pp3r!"; // TODO: später sicher laden

   public PasswordEncryptionService() {
      // Kein spezieller Konstruktor erforderlich
   }

   /**
    * Hash ein Passwort mit BCrypt und Pepper
    * @param password das rohe Passwort
    * @return gehashter String (inkl. Salt)
    */
   public String hashPassword(String password) {
      String pepperedPassword = password + PEPPER;
      return BCrypt.hashpw(pepperedPassword, BCrypt.gensalt());
   }

   /**
    * Vergleicht Passwort mit gespeichertem Hash (z. B. beim Login)
    * @param rawPassword das vom Benutzer eingegebene Passwort
    * @param hashedPassword der gespeicherte Hash aus der Datenbank
    * @return true, wenn das Passwort korrekt ist
    */
   public boolean checkPassword(String rawPassword, String hashedPassword) {
      String pepperedPassword = rawPassword + PEPPER;
      return BCrypt.checkpw(pepperedPassword, hashedPassword);
   }
}