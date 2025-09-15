package com.teste.cqrs_bank.api.auth;

import com.teste.cqrs_bank.api.auth.dto.LoginRequest;
import com.teste.cqrs_bank.api.auth.dto.SignupRequest;
import com.teste.cqrs_bank.api.auth.dto.TokenResponse;
import com.teste.cqrs_bank.service.AuthService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/auth")
public class AuthController {

    private final AuthService auth;

    public AuthController(AuthService auth) {
        this.auth = auth;
    }

    @PostMapping("/signup")
    public ResponseEntity<TokenResponse> signup(@Valid @RequestBody SignupRequest req) {
        String token = auth.signup(req.fullName(), req.document(), req.login(), req.password());
        return ResponseEntity.ok(new TokenResponse(token));
    }

    @PostMapping("/login")
    public ResponseEntity<TokenResponse> login(@Valid @RequestBody LoginRequest req) {
        String token = auth.login(req.login(), req.password());
        return ResponseEntity.ok(new TokenResponse(token));
    }
}
