package ch.bbw.pr.tresorbackend.config;


import ch.bbw.pr.tresorbackend.util.JwtUtil;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class JwtConfig {

    @Bean
    public JwtUtil jwtUtil() {
        String secret = "mein-geheimer-schlüssel-der-lang-genug-ist-1234567890";
        long expirationTimeMs = 3600000; // 1 Stunde in Millisekunden
        return new JwtUtil(secret, expirationTimeMs);
    }
}
