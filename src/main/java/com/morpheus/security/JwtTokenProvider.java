package com.morpheus.security;

import com.morpheus.config.JwtProperties;
import io.jsonwebtoken.*;
import io.jsonwebtoken.security.Keys;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.nio.charset.StandardCharsets;
import java.security.Key;
import java.util.Date;
import java.util.List;
import java.util.Arrays;

@Component
@RequiredArgsConstructor
public class JwtTokenProvider {

    private final JwtProperties jwtProperties;
    private Key secretKey;

    @PostConstruct
    protected void init() {
        String secret = jwtProperties.getSecret();
        if (secret.length() < 32) {
            throw new IllegalArgumentException("A chave secreta deve ter pelo menos 32 caracteres para ser segura.");
        }
        this.secretKey = Keys.hmacShaKeyFor(secret.getBytes(StandardCharsets.UTF_8));
    }

    public String generateToken(String subject) {
        Date now = new Date();
        Date expiration = new Date(now.getTime() + jwtProperties.getExpiration() * 1000L);

        return Jwts.builder().setSubject(subject).setIssuedAt(now).setExpiration(expiration).signWith(secretKey, SignatureAlgorithm.HS256).compact();
    }

    public String generateToken(String subject, List<String> roles) {
        Date now = new Date();
        Date expiration = new Date(now.getTime() + jwtProperties.getExpiration() * 1000L);
        return Jwts.builder().setSubject(subject).claim("roles", String.join(",", roles)).setIssuedAt(now).setExpiration(expiration).signWith(secretKey, SignatureAlgorithm.HS256).compact();
    }

    public boolean validateToken(String token) {
        try {
            Jwts.parserBuilder().setSigningKey(secretKey).build().parseClaimsJws(token);
            return true;
        } catch (JwtException | IllegalArgumentException ex) {
            return false;
        }
    }

    public String getSubject(String token) {
        return Jwts.parserBuilder().setSigningKey(secretKey).build().parseClaimsJws(token).getBody().getSubject();
    }

    public List<String> getRoles(String token) {
        Claims claims = Jwts.parserBuilder().setSigningKey(secretKey).build().parseClaimsJws(token).getBody();
        Object rolesClaim = claims.get("roles");
        if (rolesClaim instanceof String rolesStr) {
            return Arrays.asList(rolesStr.split(","));
        } else if (rolesClaim instanceof java.util.Collection<?> rolesCol) {
            return rolesCol.stream().map(Object::toString).toList();
        }
        return List.of();
    }
}
