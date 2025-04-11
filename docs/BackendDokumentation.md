# BackendDokumentation zu Tresor App

## Passwortsicherheit: Hashing, Salt & Pepper

### Was ist ein Hash?
Ein Hash ist eine Einweg-Funktion, mit der sensible Daten in einen scheinbar zufälligen Wert umgewandelt werden. Der Hash kann **nicht** zurückgerechnet werden.
### Was ist Salt?
Salt ist ein zufälliger Wert, der **für jeden Benutzer individuell generiert** wird. Er wird dem Passwort hinzugefügt, bevor es gehasht wird. Dadurch verhindern wir sogenannte Rainbow-Table-Angriffe.

### Was ist Pepper?
Pepper ist ein **geheimer, systemweiter Schlüssel**, der **nicht in der Datenbank**, sondern im Server (z. B. in `application.properties`) gespeichert wird. Er wird ebenfalls zum Passwort hinzugefügt, um Brute-Force-Angriffe zu erschweren.

---

## Warum diese Methode?

Die Anwendung soll Secrets und Zugangsdaten speichern – also besonders schützenswerte Daten. Daher dürfen Passwörter **niemals im Klartext** gespeichert werden.

Ich habe mich für die Kombination aus **Salt + Pepper + Hashing** entschieden, weil das aktuell ein bewährter Standard ist.

---

## Verwendeter Algorithmus

Ich verwende in dieser Implementierung den Algorithmus **PBKDF2WithHmacSHA256** mit Salt und Pepper. Alternativ wären auch `bcrypt`, `scrypt` oder `Argon2` möglich – aber PBKDF2 ist weit verbreitet und gut unterstützt in Java/Spring Boot.

---

## Ablauf Registrierung

1. User gibt Username und Passwort ein
2. Backend generiert ein Salt (z. B. 16 Bytes random)
3. Passwort + Salt + Pepper → Hashfunktion
4. Speichern in DB:
    - `username`
    - `hashedPassword`
    - `salt`

---

## Ablauf Login

1. User gibt Username und Passwort ein
2. Backend holt Salt & gespeicherten Hash
3. Passwort + Salt + Pepper → Hashfunktion
4. Vergleich: aktueller Hash == gespeicherter Hash → Login erlaubt

---

## Migration bestehender Klartextpasswörter

Ein separates Skript ersetzt alle Klartextpasswörter durch sichere Hashes mit Salt & Pepper. Danach sind keine unverschlüsselten Passwörter mehr in der DB.

---

## Anwendung von `Pepper` in Spring Boot

In `application.properties`:

```properties
tresor.pepper=G3h31@%20asj!
