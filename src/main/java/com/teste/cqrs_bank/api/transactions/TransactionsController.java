package com.teste.cqrs_bank.api.transactions;

import com.teste.cqrs_bank.api.transactions.dto.AmountRequest;
import com.teste.cqrs_bank.service.TransactionService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import jakarta.validation.Valid;

@RestController
@RequestMapping("/transactions")
public class TransactionsController {

    private final TransactionService svc;

    public TransactionsController(TransactionService svc) {
        this.svc = svc;
    }

    @PostMapping("/deposit")
    public ResponseEntity<?> deposit(Authentication auth, @Valid @RequestBody AmountRequest req) {
        var acc = svc.deposit((String) auth.getPrincipal(), req.amount());
        return ResponseEntity.ok().body(java.util.Map.of("balance", acc.getBalance()));
    }

    @PostMapping("/pay-bill")
    public ResponseEntity<?> payBill(Authentication auth, @Valid @RequestBody AmountRequest req) {
        var acc = svc.payBill((String) auth.getPrincipal(), req.amount());
        return ResponseEntity.ok().body(java.util.Map.of("balance", acc.getBalance()));
    }
}
