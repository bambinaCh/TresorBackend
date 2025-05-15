# Backend-Dokumentation zur Tresor-App

## Passwortsicherheit mit Hashing, Salt und Pepper

### Was ist ein Hash?
Ein Hash ist eine Einweg-Funktion, die ein Passwort in eine scheinbar zufällige Zeichenkette umwandelt. Der Hash kann nicht zurückgerechnet werden. Dadurch bleiben Passwoerter auch bei einem Datenbankleck geheim – sofern korrekt gehasht.

### Was ist Salt?
Salt ist ein zufaelliger Wert, der vor dem Hashen zum Passwort hinzugefuegt wird. Er sorgt dafuer, dass selbst identische Passwoerter unterschiedliche Hashes erzeugen.  
Bei der Verwendung von `BCrypt` wird der Salt automatisch generiert und im Hash mitgespeichert.

### Was ist Pepper?
Pepper ist ein zusaetzlicher geheimer Schluessel, der systemweit definiert ist – zum Beispiel im Code oder in der `application.properties`. Er wird vor dem Hashing an das Passwort angehaengt, ist jedoch **nicht in der Datenbank gespeichert**.  
Er erhoeht die Sicherheit bei Brute-Force- oder Rainbow-Table-Angriffen zusaetzlich.

---

## Warum wird diese Methode eingesetzt?

Die Tresor-App speichert hochsensible Daten wie Passwoerter, Logins oder Kreditkarteninformationen. Deshalb ist es absolut notwendig, dass Benutzerpasswoerter **niemals im Klartext gespeichert** werden.

Die Kombination von `BCrypt` mit Pepper bietet:
- Schutz gegen Rainbow Tables
- Schutz vor Brute-Force-Angriffen
- Automatische Verwendung von Salt

---

## Verwendeter Algorithmus

In dieser Anwendung wird der Algorithmus `BCrypt` verwendet. Er ist:
- in Spring Boot direkt unterstuetzt (`org.springframework.security.crypto.bcrypt.BCrypt`)
- langsam genug fuer Sicherheit (Schutz vor schnellen Angriffsversuchen)
- automatisch mit Salt ausgestattet

---

## Ablauf bei Registrierung

1. Der Benutzer gibt ein Passwort ein.
2. Das Passwort wird mit dem Pepper kombiniert: `passwort + pepper`
3. `BCrypt.hashpw()` generiert den Hash (inkl. Salt)
4. Der Hash wird in der Datenbank gespeichert.

---

## Ablauf bei Login

1. Der Benutzer gibt sein Passwort ein.
2. Das eingegebene Passwort wird erneut mit dem Pepper kombiniert.
3. `BCrypt.checkpw()` vergleicht das Ergebnis mit dem gespeicherten Hash.
4. Stimmen die Hashes ueberein, ist der Login erfolgreich.

---

## Anwendung von Pepper in Spring Boot

In `application.properties` kann der Pepper wie folgt definiert werden:

```properties
tresor.pepper=G3h31@%20asj!
```

## Passwort-Hashing bei Registrierung
Das passiert im UserController, und dort wird der PasswordEncryptionService verwendet.

Hier ist, was im Code passiert:

```
@Autowired
private PasswordEncryptionService encryptionService;


@PostMapping("/register")
public ResponseEntity<User> registerUser(@RequestBody User newUser) {
    newUser.setPassword(encryptionService.hashPassword(newUser.getPassword()));
    userRepository.save(newUser);
    return ResponseEntity.ok(newUser);
}
```

