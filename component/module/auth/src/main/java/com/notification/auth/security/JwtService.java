package com.notification.auth.security;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import org.springframework.stereotype.Service;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.util.Date;

@Service
public class JwtService {

    private final JwtProperties props;
    private final SecretKey key;

    public JwtService(JwtProperties props) {
        this.props = props;
        this.key = Keys.hmacShaKeyFor(props.getSecret().getBytes(StandardCharsets.UTF_8));
    }

    public String generateToken(String subject) {
        final String normalized = (subject == null) ? "" : subject.trim().toLowerCase();

        Instant now = Instant.now();
        Instant exp = now.plusSeconds(props.getExpirationMinutes() * 60);

        return Jwts.builder()
                .issuer(props.getIssuer())
                .subject(normalized)
                .issuedAt(Date.from(now))
                .expiration(Date.from(exp))
                .signWith(key)
                .compact();
    }

    /**
     * Use no Filter: valida assinatura + expiração + issuer.
     */
    public String extractSubjectValidated(String token) {
        try {
            return parseAllClaimsValidated(token).getSubject();
        } catch (ExpiredJwtException e) {
            throw new JwtInvalidException("Token expired");
        } catch (JwtException | IllegalArgumentException e) {
            throw new JwtInvalidException("Token invalid");
        }
    }

    /**
     * Mantém método simples (se você usar em outros lugares),
     * mas recomendo usar extractSubjectValidated no fluxo de autenticação.
     */
    public String extractSubject(String token) {
        return parseAllClaimsValidated(token).getSubject();
    }

    public boolean isTokenValid(String token) {
        try {
            parseAllClaimsValidated(token);
            return true;
        } catch (Exception ignored) {
            return false;
        }
    }

    private Claims parseAllClaimsValidated(String token) {
        // Aqui está a melhoria principal: exige issuer correto além da assinatura.
        return Jwts.parser()
                .verifyWith(key)
                .requireIssuer(props.getIssuer())
                .build()
                .parseSignedClaims(token)
                .getPayload();
    }

    public static class JwtInvalidException extends RuntimeException {
        public JwtInvalidException(String message) {
            super(message);
        }
    }
}
