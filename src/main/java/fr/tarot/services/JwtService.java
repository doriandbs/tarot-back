package fr.tarot.services;


import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.nio.charset.StandardCharsets;
import java.security.Key;
import java.time.Instant;
import java.util.Date;
import java.util.UUID;

@Service
public class JwtService {

    private final Key key;
    private final String issuer;
    private final long accessTokenMinutes;

    public JwtService(
            @Value("${app.jwt.secret}") String secret,
            @Value("${app.jwt.issuer}") String issuer,
            @Value("${app.jwt.accessTokenMinutes}") long accessTokenMinutes
    ) {
        if (secret == null || secret.length() < 32) {
            throw new IllegalArgumentException("JWT secret must be at least 32 characters.");
        }
        this.key = Keys.hmacShaKeyFor(secret.getBytes(StandardCharsets.UTF_8));
        this.issuer = issuer;
        this.accessTokenMinutes = accessTokenMinutes;
    }

    public String generateAccessToken(UUID userId, String email) {
        Instant now = Instant.now();
        Instant exp = now.plusSeconds(accessTokenMinutes * 60);

        return Jwts.builder()
                .issuer(issuer)
                .subject(userId.toString())
                .claim("email", email)
                .issuedAt(Date.from(now))
                .expiration(Date.from(exp))
                .signWith(key)
                .compact();
    }

    public String generatePasswordResetToken(UUID userId, String email) {
        Instant now = Instant.now();
        Instant exp = now.plusSeconds(15 * 60);

        return Jwts.builder()
                .issuer(issuer)
                .subject(userId.toString())
                .claim("email", email)
                .claim("typ", "password_reset")
                .issuedAt(Date.from(now))
                .expiration(Date.from(exp))
                .signWith(key)
                .compact();
    }

    public Claims parse(String token) {
        return Jwts.parser()
                .verifyWith((javax.crypto.SecretKey) key)
                .requireIssuer(issuer)
                .build()
                .parseSignedClaims(token)
                .getPayload();
    }
}
