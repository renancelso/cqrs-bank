package com.teste.cqrs_bank.api.auth;

import com.teste.cqrs_bank.api.auth.dto.LoginRequest;
import com.teste.cqrs_bank.api.auth.dto.SignupRequest;
import com.teste.cqrs_bank.api.auth.dto.TokenResponse;
import com.teste.cqrs_bank.service.AuthService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * Endpoints de autenticação/cadastro (JWT).
 *
 * <ul>
 *   <li><b>POST /auth/signup</b>: cria usuário e retorna token JWT.</li>
 *   <li><b>POST /auth/login</b>: autentica e retorna token JWT.</li>
 * </ul>
 *
 * <p>Rotas de transação/consulta exigem <code>Authorization: Bearer &lt;jwt&gt;</code>.</p>
 *
 * @since 1.0
 */
@RestController
@RequestMapping("/auth")
public class AuthController {

    private final AuthService auth;

    public AuthController(AuthService auth) {
        this.auth = auth;
    }

    /**
     * Cadastra usuário e retorna JWT. Rota: POST /auth/signup
     */
    @PostMapping("/signup")
    public ResponseEntity<TokenResponse> signup(@Valid @RequestBody SignupRequest req) {
        String token = auth.signup(req.fullName(), req.document(), req.login(), req.password());
        return ResponseEntity.ok(new TokenResponse(token));
    }

    /**
     * Autentica usuário e retorna JWT. Rota: POST /auth/login
     */
    @PostMapping("/login")
    public ResponseEntity<TokenResponse> login(@Valid @RequestBody LoginRequest req) {
        String token = auth.login(req.login(), req.password());
        return ResponseEntity.ok(new TokenResponse(token));
    }
}
