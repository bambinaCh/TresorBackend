# Backend-Dokumentation zur Tresor-App

## Passwortsicherheit mit Hashing, Salt und Pepper

### Was ist ein Hash?
EEinweg-Funktion, die ein Passwort in eine scheinbar zufällige Zeichenkette umwandelt. Der Hash kann nicht zurückgerechnet werden. 

### Was ist Salt?
Zufaelliger Wert, der vor dem Hashen zum Passwort hinzugefuegt wird. Er sorgt dafuer, dass selbst identische Passwoerter unterschiedliche Hashes erzeugen.  
Bei der Verwendung von `BCrypt` wird der Salt automatisch generiert und im Hash mitgespeichert.

### Was ist Pepper?
Pepper ist ein zusaetzlicher geheimer Schluessel,Code oder in der `application.properties`. Er wird vor dem Hashing an das Passwort angehaengt, ist jedoch **nicht in der Datenbank gespeichert**.  
Er erhoeht die Sicherheit bei Brute-Force- oder Rainbow-Table-Angriffen zusaetzlich.

---

### Warum wird diese Methode eingesetzt?

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

### Was ist BCrypt?

Ein kryptografischer Hashing-Algorithmus, der speziell für die sichere Speicherung von Passwörtern entwickelt wurde.


### Warum nicht SHA-256 oder MD5?
Hash-Verfahren wie SHA-256 oder MD5 sind für Geschwindigkeit optimiert – was bei Passwörtern ein Nachteil ist:
- Sie sind **viel zu schnell**
- Können mit GPUs sehr effizient „gecrackt“ werden
- Haben **kein eingebautes Salt**

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

### Wo wird das Secret verschlüsselt und entschlüsselt?
Die Verschlüsselung und Entschlüsselung passiert im SecretController.java. Und zwar hier:

🔐 Verschlüsselung beim Speichern
```
String encrypted = new EncryptUtil(newSecret.getEncryptPassword()).encrypt(newSecret.getContent().toString());

Secret secret = new Secret(
null,
user.getId(),
encrypted
);

secretService.createSecret(secret);
```
🔸 Das passiert in der Methode createSecret2(...)
🔸 Der Klartext (JsonNode content) wird mit dem Passwort (encryptPassword) verschlüsselt
🔸 Danach wird encrypted (als String) in der DB gespeichert – das ist korrekt


### Speicherung von Secrets in der Datenbank
Secrets wie Notizen, Kreditkarten oder Passwörter werden verschlüsselt gespeichert

Dafür verwenden wir eine symmetrische Verschlüsselung mit AES (Advanced Encryption Standard)

Ablauf:
- Benutzer gibt ein Passwort ein, das nur zum Verschlüsseln dient (encryptPassword)

- Aus diesem Passwort wird ein AES-Schlüssel abgeleitet

- Der Secret-Inhalt wird verschlüsselt und als Base64-Text gespeichert

- Beim Abruf wird derselbe Schlüssel verwendet, um den Inhalt wieder zu entschlüsseln

### Warum AES und kein Hash?
Weil der Inhalt wiederhergestellt werden muss (also entschlüsselt, nicht nur überprüft)

Hashing ist eine Einwegfunktion, Verschlüsselung ist umkehrbar

### Sicherheitshinweis:
Das Verschlüsselungspasswort sollte nicht identisch mit dem Login-Passwort sein

Der AES-Schlüssel wird nie gespeichert, sondern aus Benutzereingabe erzeugt