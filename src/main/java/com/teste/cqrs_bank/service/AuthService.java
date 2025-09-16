package com.teste.cqrs_bank.service;

import com.teste.cqrs_bank.domain.account.Account;
import com.teste.cqrs_bank.domain.account.AccountRepository;
import com.teste.cqrs_bank.domain.user.User;
import com.teste.cqrs_bank.domain.user.UserRepository;
import com.teste.cqrs_bank.security.JwtTokenProvider;
import jakarta.transaction.Transactional;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.Objects;

/**
 * Serviço de autenticação/cadastro.
 * - signup: cria User/Account e retorna JWT
 * - login: valida credenciais e retorna JWT
 * @since 1.0
 */
@Service
public class AuthService {

    private final UserRepository userRepository;
    private final AccountRepository accountRepository;
    private final JwtTokenProvider jwt;
    private final BCryptPasswordEncoder passwordEncoder = new BCryptPasswordEncoder();

    public AuthService(UserRepository userRepository,
                       AccountRepository accountRepository,
                       JwtTokenProvider jwt) {
        this.userRepository = userRepository;
        this.accountRepository = accountRepository;
        this.jwt = jwt;
    }

    /** Cria usuário/conta e retorna JWT. */
    @Transactional
    public String signup(String fullName, String document, String login, String rawPassword) {
        Objects.requireNonNull(fullName); Objects.requireNonNull(document);
        Objects.requireNonNull(login); Objects.requireNonNull(rawPassword);

        if (userRepository.existsByLogin(login)) {
            throw new IllegalArgumentException("Login já utilizado.");
        }

        var user = User.builder()
                .fullName(fullName)
                .document(document)
                .login(login)
                .passwordHash(passwordEncoder.encode(rawPassword))
                .build();

        user = userRepository.save(user);

        var account = Account.builder()
                .user(user)
                .build(); // balance = 0 no @PrePersist
        accountRepository.save(account);

        return jwt.generateToken(user.getId(), user.getLogin());
    }

    /** Autentica usuário e retorna JWT. */
    @Transactional
    public String login(String login, String rawPassword) {
        var user = userRepository.findByLogin(login)
                .orElseThrow(() -> new IllegalArgumentException("Credenciais inválidas."));

        if (!passwordEncoder.matches(rawPassword, user.getPasswordHash())) {
            throw new IllegalArgumentException("Credenciais inválidas.");
        }

        return jwt.generateToken(user.getId(), user.getLogin());
    }
}
