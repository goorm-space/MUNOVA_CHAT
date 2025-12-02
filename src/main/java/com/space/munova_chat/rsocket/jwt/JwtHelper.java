package com.space.munova_chat.rsocket.jwt;

import com.space.munova_chat.rsocket.entity.MemberRole;
import com.space.munova_chat.rsocket.exception.JwtAuthException;
import io.jsonwebtoken.*;
import io.jsonwebtoken.security.Keys;
import jakarta.annotation.PostConstruct;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;

@Component
public class JwtHelper {
    @Value("${jwt.secret}")
    private String secret;

    private SecretKey secretKey;

    @PostConstruct
    public void init() {
        byte[] keyBytes = secret.getBytes(StandardCharsets.UTF_8);
        secretKey = Keys.hmacShaKeyFor(keyBytes);
    }

    public Claims parseClaims(String token) {
        try {
            return Jwts.parser()
                    .verifyWith(secretKey)
                    .build()
                    .parseSignedClaims(token)
                    .getPayload();

        } catch (SecurityException | MalformedJwtException e) {
            throw JwtAuthException.invalidSignature();
        } catch (ExpiredJwtException e) {
            throw JwtAuthException.expired();
        } catch (UnsupportedJwtException e) {
            throw JwtAuthException.unsupported();
        } catch (IllegalArgumentException e) {
            throw JwtAuthException.illegal();
        }
    }

    public void validateJwt(String token) {
        parseClaims(token);
    }

    public Long getMemberId(Claims claims) {
        return Long.parseLong(claims.getSubject());
    }

    public MemberRole getRole(Claims claims) {
        return MemberRole.fromCode(claims.get("authorities", String.class));
    }
}
