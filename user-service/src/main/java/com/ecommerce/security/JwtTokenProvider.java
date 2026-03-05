package com.ecommerce.security;

import com.ecommerce.model.entity.User;
import io.jsonwebtoken.*;
import io.jsonwebtoken.security.Keys;
import io.jsonwebtoken.security.SecurityException;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.util.Date;

@Slf4j
@Component
@RequiredArgsConstructor
public class JwtTokenProvider {

    @Value("${jwt.secret}")
    private String jwtSecret;

    @Value("${jwt.expiration}")
    private long jwtExpiration;

    private SecretKey key;

    @PostConstruct
    protected void init() {
        try {
            this.key = Keys.hmacShaKeyFor(jwtSecret.getBytes(StandardCharsets.UTF_8));
            log.info("JwtTokenProvider успешно инициализирован. Длина секрета: {} символов", jwtSecret.length());

            if (jwtSecret.length() < 32) {
                log.warn("Секретный ключ слишком короткий. Рекомендуется использовать минимум 32 символа.");
            }
        } catch (Exception e) {
            log.error("Ошибка инициализации JwtTokenProvider: {}", e.getMessage(), e);
            throw new RuntimeException("Не удалось инициализировать JWT провайдер", e);
        }
    }

    public String generateToken(User user) {
        log.info("Генерация токена для пользователя: {}", user.getUsername());
        Date now = new Date();
        Date expiryDate = new Date(now.getTime() + jwtExpiration);

        return Jwts.builder()
                .subject(user.getId().toString())
                .claim("username", user.getUsername())
                .claim("email", user.getEmail())
                .claim("firstName", user.getFirstName())
                .claim("lastName", user.getLastName())
                .claim("role", user.getRole().toString())
                .issuedAt(now)
                .expiration(expiryDate)
                .signWith(key, Jwts.SIG.HS256)
                .compact();
    }

    public boolean validateToken(String token) {
        try {
            Jwts.parser()
                    .verifyWith(key)
                    .build()
                    .parseSignedClaims(token);
            log.debug("Токен валиден");
            return true;
        } catch (SecurityException ex) {
            log.error("Неверная подпись JWT токена: {}", ex.getMessage());
        } catch (MalformedJwtException ex) {
            log.error("Неверный формат JWT токена: {}", ex.getMessage());
        } catch (ExpiredJwtException ex) {
            log.error("JWT токен истек: {}", ex.getMessage());
        } catch (UnsupportedJwtException ex) {
            log.error("Неподдерживаемый JWT токен: {}", ex.getMessage());
        } catch (IllegalArgumentException ex) {
            log.error("JWT claims строка пуста: {}", ex.getMessage());
        }
        return false;
    }

    public Long getUserIdFromToken(String token) {
        try {
            Claims claims = Jwts.parser()
                    .verifyWith(key)
                    .build()
                    .parseSignedClaims(token)
                    .getPayload();

            return Long.parseLong(claims.getSubject());
        } catch (Exception ex) {
            log.warn("Не удалось извлечь userId из токена: {}", ex.getMessage());
            return null;
        }
    }

    public String getUsernameFromToken(String token) {
        try {
            return Jwts.parser()
                    .verifyWith(key)
                    .build()
                    .parseSignedClaims(token)
                    .getPayload()
                    .getSubject();
        } catch (Exception ex) {
            log.warn("Не удалось извлечь username из токена: {}", ex.getMessage());
            return null;
        }
    }

}
