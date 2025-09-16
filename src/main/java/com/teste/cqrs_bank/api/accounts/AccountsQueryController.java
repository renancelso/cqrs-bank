package com.teste.cqrs_bank.api.accounts;

import com.teste.cqrs_bank.read.view.AccountView;
import com.teste.cqrs_bank.read.view.AccountViewRepository;
import com.teste.cqrs_bank.domain.account.AccountRepository;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

/**
 * Controlador de consulta (Query Side). Lê somente o Read Model (Mongo)
 * e retorna o resumo da conta já no formato exigido pela UI.
 *
 * <p>Se o usuário ainda não tem projeção no Mongo, retorna:
 * <code>{"SaldoTotal":"0.00","Historico":[]}</code>.</p>
 *
 * @since 1.0
 */
@RestController
@RequestMapping("/accounts")
public class AccountsQueryController {

    private final AccountRepository accountRepo;
    private final AccountViewRepository viewRepo;

    public AccountsQueryController(AccountRepository accountRepo, AccountViewRepository viewRepo) {
        this.accountRepo = accountRepo;
        this.viewRepo = viewRepo;
    }

    /**
     * Retorna o snapshot do usuário autenticado a partir do Mongo, com chaves e
     * valores formatados:
     * {
     *   "SaldoTotal": "0.00",
     *   "Historico": [
     *     { "type": "deposito"|"saque", "valor": "0.00", "data": "dd-MM-yyyy HH:mm:ss" }
     *   ]
     * }
     * Rota: GET /accounts/me/summary
     */
    @GetMapping("/me/summary")
    public ResponseEntity<?> getSummary(Authentication auth) {
        String userId = (String) auth.getPrincipal();
        var acc = accountRepo.findByUserId(userId).orElseThrow();
        AccountView view = viewRepo.findById(acc.getId()).orElse(null);
        if (view == null) {

            return ResponseEntity.ok(java.util.Map.of(
                    "SaldoTotal", "0.00",
                    "Historico", java.util.List.of()
            ));
        }
        return ResponseEntity.ok(java.util.Map.of(
                "SaldoTotal", view.getSaldoTotal(),
                "Historico", view.getHistorico()
        ));
    }
}
