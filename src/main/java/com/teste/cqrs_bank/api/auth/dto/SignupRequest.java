package com.teste.cqrs_bank.api.auth.dto;

import jakarta.validation.constraints.NotBlank;
import org.hibernate.validator.constraints.br.CPF;

public record SignupRequest(
        @NotBlank String fullName,
        @CPF String document,
        @NotBlank String login,
        @NotBlank String password
) {}
