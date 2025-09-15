package com.teste.cqrs_bank.service;

import com.teste.cqrs_bank.domain.account.Account;
import com.teste.cqrs_bank.domain.account.AccountRepository;
import com.teste.cqrs_bank.domain.transaction.Transaction;
import com.teste.cqrs_bank.domain.transaction.TransactionRepository;
import com.teste.cqrs_bank.domain.transaction.TxType;
import com.teste.cqrs_bank.write.events.DomainEventPublisher;
import jakarta.transaction.Transactional;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;

@Service
public class TransactionService {

    private static final BigDecimal ONE_HUNDRED = new BigDecimal("100");
    private static final BigDecimal INTEREST = new BigDecimal("0.02"); // 2%
    private static final RoundingMode RM = RoundingMode.HALF_EVEN;

    private final AccountRepository accountRepository;
    private final TransactionRepository transactionRepository;
    private final DomainEventPublisher eventPublisher;

    public TransactionService(AccountRepository accountRepository,
                              TransactionRepository transactionRepository,
                              DomainEventPublisher eventPublisher) {
        this.accountRepository = accountRepository;
        this.transactionRepository = transactionRepository;
        this.eventPublisher = eventPublisher;
    }

    @Transactional
    public Account deposit(String userId, BigDecimal amount) {
        if (amount == null || amount.compareTo(BigDecimal.ZERO) <= 0) {
            throw new IllegalArgumentException("Valor de depósito deve ser positivo.");
        }
        amount = amount.setScale(2, RM);

        var account = accountRepository.findByUserIdForUpdate(userId)
                .orElseThrow(() -> new IllegalArgumentException("Conta não encontrada para o usuário."));

        BigDecimal remaining = amount;
        BigDecimal balance = account.getBalance();

        if (balance.compareTo(BigDecimal.ZERO) < 0) {

            BigDecimal debt = balance.abs(); // principal
            BigDecimal principalPaid = remaining.min(debt).setScale(2, RM);

            account.setBalance(balance.add(principalPaid));
            remaining = remaining.subtract(principalPaid).setScale(2, RM);

            BigDecimal interest = principalPaid.multiply(INTEREST).setScale(2, RM);
            BigDecimal interestCharged = remaining.min(interest).setScale(2, RM);
            remaining = remaining.subtract(interestCharged).setScale(2, RM);

            BigDecimal paymentRecorded = principalPaid.add(interestCharged).setScale(2, RM);
            if (paymentRecorded.compareTo(BigDecimal.ZERO) > 0) {
                transactionRepository.save(Transaction.builder()
                        .account(account)
                        .type(TxType.BILL_PAYMENT)
                        .amount(interestCharged)
                        .build());
            }
        }

        transactionRepository.save(Transaction.builder()
                .account(account)
                .type(TxType.DEPOSIT)
                .amount(amount)
                .build());
        account.setBalance(account.getBalance().add(remaining));

        var saved = accountRepository.save(account);
        eventPublisher.publishTransactionEvent(saved.getId());
        return saved;
    }

    @Transactional
    public Account payBill(String userId, BigDecimal amount) {
        if (amount == null || amount.compareTo(BigDecimal.ZERO) <= 0) {
            throw new IllegalArgumentException("Valor do pagamento deve ser positivo.");
        }
        amount = amount.setScale(2, RM);

        var account = accountRepository.findByUserIdForUpdate(userId)
                .orElseThrow(() -> new IllegalArgumentException("Conta não encontrada para o usuário."));

        // debita, podendo negativar
        transactionRepository.save(Transaction.builder()
                .account(account)
                .type(TxType.BILL_PAYMENT)
                .amount(amount)
                .build());

        account.setBalance(account.getBalance().subtract(amount));
        var saved = accountRepository.save(account);

        // publica evento para projeção
        eventPublisher.publishTransactionEvent(saved.getId());
        return saved;
    }
}
