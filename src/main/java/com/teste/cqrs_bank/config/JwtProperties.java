package com.teste.cqrs_bank.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * Propriedades do JWT (secret, expirationMinutes) carregadas de app.security.jwt.* @since 1.0
 */
@ConfigurationProperties(prefix = "app.security.jwt")
public record JwtProperties(
        String secret,
        Integer expirationMinutes
) {
}
