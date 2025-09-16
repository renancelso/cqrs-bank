// (substitua pelo abaixo se o seu não tiver parse/validate)
package com.teste.cqrs_bank.security;

import com.teste.cqrs_bank.config.JwtProperties;
import io.jsonwebtoken.*;
import io.jsonwebtoken.security.Keys;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Date;

/**
 * Geração e validação de JWT (HS256), usando segredo e expiração de {@link com.teste.cqrs_bank.config.JwtProperties}.
 * Exponde <code>generateToken(userId, login)</code> e <code>parse(token)</code>.
 *
 * @since 1.0
 */
@Component
public class JwtTokenProvider {

    private final SecretKey key;
    private final int expirationMinutes;

    public JwtTokenProvider(JwtProperties props) {
        this.key = Keys.hmacShaKeyFor(props.secret().getBytes(StandardCharsets.UTF_8));
        this.expirationMinutes = props.expirationMinutes();
    }

    public String generateToken(String userId, String login) {
        Instant now = Instant.now();
        Instant exp = now.plus(expirationMinutes, ChronoUnit.MINUTES);

        return Jwts.builder()
                .setSubject(userId)
                .claim("login", login)
                .setIssuedAt(Date.from(now))
                .setExpiration(Date.from(exp))
                .signWith(key, SignatureAlgorithm.HS256)
                .compact();
    }

    public Jws<Claims> parse(String token) throws JwtException {
        return Jwts.parserBuilder().setSigningKey(key).build().parseClaimsJws(token);
    }
}
