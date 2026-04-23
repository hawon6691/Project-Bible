package com.projectbible.post.common.security;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.util.Date;
import javax.crypto.SecretKey;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

@Service
public class TokenService {
    private final SecretKey secretKey;

    public TokenService(@Value("${JWT_SECRET:change-me-access}") String jwtSecret) {
        this.secretKey = Keys.hmacShaKeyFor(jwtSecret.repeat(Math.max(1, 32 / Math.max(jwtSecret.length(), 1) + 1)).substring(0, 32).getBytes(StandardCharsets.UTF_8));
    }

    public String createAccessToken(CurrentActor actor, long expiresInSeconds) {
        Instant now = Instant.now();
        return Jwts.builder()
            .subject(String.valueOf(actor.id()))
            .claim("email", actor.email())
            .claim("role", actor.role())
            .claim("subjectType", actor.subjectType())
            .issuedAt(Date.from(now))
            .expiration(Date.from(now.plusSeconds(expiresInSeconds)))
            .signWith(secretKey)
            .compact();
    }

    public CurrentActor parse(String token) {
        Claims claims = Jwts.parser().verifyWith(secretKey).build().parseSignedClaims(token).getPayload();
        return new CurrentActor(
            Long.valueOf(claims.getSubject()),
            String.valueOf(claims.get("email")),
            String.valueOf(claims.get("role")),
            String.valueOf(claims.get("subjectType"))
        );
    }
}
