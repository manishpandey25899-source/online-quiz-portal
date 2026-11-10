package com.aiquiz.quizportal.config;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.util.Date;

@Component
public class JwtUtil {

    private final SecretKey key;
    private final long expirationMs;

    public JwtUtil(
            @Value("${jwt.secret:AIQuizPortalSuperSecretKey2026ForBcaCollegeProjectDevelopmentOnly1234567890}") String secret,
            @Value("${jwt.expiration:86400000}") long expirationMs
    ) {
        this.key = Keys.hmacShaKeyFor(secret.getBytes(StandardCharsets.UTF_8));
        this.expirationMs = expirationMs;
    }

    public String generateToken(Integer userId, String email, String role, String name) {
        Date now = new Date();
        Date expiryDate = new Date(now.getTime() + expirationMs);

        return Jwts.builder()
                .subject(email)
                .claim("userId", userId)
                .claim("role", role != null ? role.toLowerCase() : "student")
                .claim("name", name != null ? name : "")
                .issuedAt(now)
                .expiration(expiryDate)
                .signWith(key)
                .compact();
    }

    public String generateToken(Integer userId, String email, String role) {
        return generateToken(userId, email, role, "");
    }

    public Claims getClaims(String token) {
        return Jwts.parser()
                .verifyWith(key)
                .build()
                .parseSignedClaims(token)
                .getPayload();
    }

    public boolean validateToken(String token) {
        try {
            Claims claims = getClaims(token);
            return claims.getExpiration().after(new Date());
        } catch (Exception e) {
            return false;
        }
    }

    public String getEmailFromToken(String token) {
        return getClaims(token).getSubject();
    }

    public Integer getUserIdFromToken(String token) {
        Object val = getClaims(token).get("userId");
        if (val instanceof Number) {
            return ((Number) val).intValue();
        }
        return null;
    }

    public String getRoleFromToken(String token) {
        Object val = getClaims(token).get("role");
        return val != null ? val.toString() : "student";
    }
}
