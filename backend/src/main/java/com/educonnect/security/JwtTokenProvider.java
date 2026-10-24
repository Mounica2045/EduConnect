package com.educonnect.security;

import io.jsonwebtoken.*;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.util.Date;

/**
 * Utility component for generating, signing, and validating JWT tokens.
 */
@Component
public class JwtTokenProvider {

    private static final Logger logger = LoggerFactory.getLogger(JwtTokenProvider.class);

    private final String jwtSecret;
    private final long jwtExpirationMs;
    private final SecretKey key;

    public JwtTokenProvider(
            @Value("${app.jwt.secret}") String jwtSecret,
            @Value("${app.jwt.expiration-ms:86400000}") long jwtExpirationMs
    ) {
        this.jwtSecret = jwtSecret;
        this.jwtExpirationMs = jwtExpirationMs;
        this.key = initializeKey(jwtSecret);
    }

    private SecretKey initializeKey(String secret) {
        try {
            // Attempt Base64 decode first
            byte[] keyBytes = Decoders.BASE64.decode(secret);
            if (keyBytes.length >= 32) {
                return Keys.hmacShaKeyFor(keyBytes);
            }
        } catch (Exception ignored) {
            // Fallback to raw bytes if not valid Base64
        }
        byte[] rawBytes = secret.getBytes(StandardCharsets.UTF_8);
        if (rawBytes.length < 32) {
            // Pad to 32 bytes minimum for HMAC-SHA256
            byte[] padded = new byte[32];
            System.arraycopy(rawBytes, 0, padded, 0, Math.min(rawBytes.length, 32));
            return Keys.hmacShaKeyFor(padded);
        }
        return Keys.hmacShaKeyFor(rawBytes);
    }

    /**
     * Generates a signed JWT token from an authenticated UserPrincipal.
     */
    public String generateToken(Authentication authentication) {
        UserPrincipal userPrincipal = (UserPrincipal) authentication.getPrincipal();
        Date now = new Date();
        Date expiryDate = new Date(now.getTime() + jwtExpirationMs);

        return Jwts.builder()
                .subject(userPrincipal.getUsername())
                .claim("userId", userPrincipal.getId())
                .claim("role", userPrincipal.getRole().name())
                .issuedAt(now)
                .expiration(expiryDate)
                .signWith(key)
                .compact();
    }

    /**
     * Extracts username (email) from JWT token.
     */
    public String getUsernameFromJwt(String token) {
        Claims claims = Jwts.parser()
                .verifyWith(key)
                .build()
                .parseSignedClaims(token)
                .getPayload();

        return claims.getSubject();
    }

    /**
     * Validates JWT token integrity and expiration.
     */
    public boolean validateToken(String authToken) {
        try {
            Jwts.parser()
                    .verifyWith(key)
                    .build()
                    .parseSignedClaims(authToken);
            return true;
        } catch (SecurityException | MalformedJwtException e) {
            logger.warn("Invalid JWT signature or malformed token");
        } catch (ExpiredJwtException e) {
            logger.warn("Expired JWT token");
        } catch (UnsupportedJwtException e) {
            logger.warn("Unsupported JWT token");
        } catch (IllegalArgumentException e) {
            logger.warn("JWT claims string is empty or null");
        }
        return false;
    }
}
