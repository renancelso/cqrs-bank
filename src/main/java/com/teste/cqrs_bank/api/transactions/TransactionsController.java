package com.teste.cqrs_bank.api.transactions;

import com.teste.cqrs_bank.api.transactions.dto.AmountRequest;
import com.teste.cqrs_bank.service.TransactionService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * Controlador de comandos financeiros (Write Model).
 *
 * <p>Encaminha a operação para o {@link com.teste.cqrs_bank.service.TransactionService}
 * e retorna o saldo numérico atualizado (<code>{"balance": ...}</code>).
 * O resumo final é obtido no Read Model via <code>/accounts/me/summary</code>.</p>
 *
 * @since 1.0
 */
@RestController
@RequestMapping("/transactions")
public class TransactionsController {

    private final TransactionService svc;

    public TransactionsController(TransactionService svc) {
        this.svc = svc;
    }

    /**
     * Lança depósito (CREDIT). Se a conta estiver negativa, o depósito quita o principal
     * e aplica 1,02% de juros sobre a parte quitada; o juro é descontado do próprio depósito
     * e a eventual sobra é creditada no saldo.
     * Rota: POST /transactions/deposit
     */
    @PostMapping("/deposit")
    public ResponseEntity<?> deposit(Authentication auth, @Valid @RequestBody AmountRequest req) {
        var acc = svc.deposit((String) auth.getPrincipal(), req.amount());
        return ResponseEntity.ok().body(java.util.Map.of("balance", acc.getBalance()));
    }

    /**
     * Paga conta (DEBIT). Pode negativar a conta.
     * Rota: POST /transactions/pay-bill
     */
    @PostMapping("/pay-bill")
    public ResponseEntity<?> payBill(Authentication auth, @Valid @RequestBody AmountRequest req) {
        var acc = svc.payBill((String) auth.getPrincipal(), req.amount());
        return ResponseEntity.ok().body(java.util.Map.of("balance", acc.getBalance()));
    }
}
