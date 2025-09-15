package com.teste.cqrs_bank.security;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jws;
import io.jsonwebtoken.JwtException;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.http.HttpHeaders;
import org.springframework.security.authentication.AbstractAuthenticationToken;
import org.springframework.security.core.authority.AuthorityUtils;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

public class JwtAuthFilter extends OncePerRequestFilter {

    private final JwtTokenProvider jwt;

    public JwtAuthFilter(JwtTokenProvider jwt) {
        this.jwt = jwt;
    }

    @Override
    protected void doFilterInternal(HttpServletRequest req, HttpServletResponse res, FilterChain chain)
            throws ServletException, IOException {

        String header = req.getHeader(HttpHeaders.AUTHORIZATION);
        if (header != null && header.startsWith("Bearer ")) {
            String token = header.substring(7);
            try {
                Jws<Claims> jws = jwt.parse(token);
                String userId = jws.getBody().getSubject();
                String login  = (String) jws.getBody().get("login");

                AbstractAuthenticationToken auth =
                        new AbstractAuthenticationToken(AuthorityUtils.NO_AUTHORITIES) {
                            @Override public Object getCredentials() { return token; }
                            @Override public Object getPrincipal() { return userId; }
                        };
                auth.setDetails(login);
                auth.setAuthenticated(true);
                SecurityContextHolder.getContext().setAuthentication(auth);

            } catch (JwtException e) {
                logger.error("token inválido/expirado → segue sem auth (Security bloqueará as rotas protegidas)");
            }
        }
        chain.doFilter(req, res);
    }
}
