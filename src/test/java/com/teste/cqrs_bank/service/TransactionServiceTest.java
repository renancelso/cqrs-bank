package com.teste.cqrs_bank.service;

import com.teste.cqrs_bank.domain.account.Account;
import com.teste.cqrs_bank.domain.account.AccountRepository;
import com.teste.cqrs_bank.domain.transaction.Transaction;
import com.teste.cqrs_bank.domain.transaction.TransactionRepository;
import com.teste.cqrs_bank.domain.transaction.TxType;
import com.teste.cqrs_bank.write.events.DomainEventPublisher;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

class TransactionServiceTest {

    AccountRepository accountRepo;
    TransactionRepository txRepo;
    DomainEventPublisher publisher;
    TransactionService service;

    @BeforeEach
    void setup() {
        accountRepo = mock(AccountRepository.class);
        txRepo = mock(TransactionRepository.class);
        publisher = mock(DomainEventPublisher.class);
        service = new TransactionService(accountRepo, txRepo, publisher);
    }

    @Test
    void deposit_quita_divida_com_juros_e_credia_resto() {
        // saldo -150, deposito 200 => paga 150 + juros 3 = 153; sobra 47 -> saldo final 47
        var userId = "u1";
        var acc = Account.builder().id("a1").balance(new BigDecimal("-150.00")).build();
        when(accountRepo.findByUserIdForUpdate(userId)).thenReturn(Optional.of(acc));
        when(accountRepo.save(any(Account.class))).thenAnswer(inv -> inv.getArgument(0));

        var saved = service.deposit(userId, new BigDecimal("200.00"));

        assertThat(saved.getBalance()).isEqualByComparingTo("47.00");

        // capturar transações salvas
        var txCaptor = ArgumentCaptor.forClass(Transaction.class);
        verify(txRepo, times(2)).save(txCaptor.capture());
        List<Transaction> txs = txCaptor.getAllValues();

        assertThat(txs.get(0).getType()).isEqualTo(TxType.BILL_PAYMENT);
        assertThat(txs.get(0).getAmount()).isEqualByComparingTo("153.00");

        assertThat(txs.get(1).getType()).isEqualTo(TxType.DEPOSIT);
        assertThat(txs.get(1).getAmount()).isEqualByComparingTo("47.00");

        verify(publisher).publishTransactionEvent("a1");
    }

    @Test
    void payBill_debita_mesmo_zerando_para_negativo() {
        var userId = "u1";
        var acc = Account.builder().id("a1").balance(new BigDecimal("0.00")).build();
        when(accountRepo.findByUserIdForUpdate(userId)).thenReturn(Optional.of(acc));
        when(accountRepo.save(any(Account.class))).thenAnswer(inv -> inv.getArgument(0));

        var saved = service.payBill(userId, new BigDecimal("150.00"));
        assertThat(saved.getBalance()).isEqualByComparingTo("-150.00");

        var txCaptor = ArgumentCaptor.forClass(Transaction.class);
        verify(txRepo).save(txCaptor.capture());
        assertThat(txCaptor.getValue().getType()).isEqualTo(TxType.BILL_PAYMENT);
        assertThat(txCaptor.getValue().getAmount()).isEqualByComparingTo("150.00");

        verify(publisher).publishTransactionEvent("a1");
    }

    @Test
    void deposit_com_saldo_positivo_soma_somente_deposito() {
        var userId = "u1";
        var acc = Account.builder().id("a1").balance(new BigDecimal("10.00")).build();
        when(accountRepo.findByUserIdForUpdate(userId)).thenReturn(Optional.of(acc));
        when(accountRepo.save(any(Account.class))).thenAnswer(inv -> inv.getArgument(0));

        var saved = service.deposit(userId, new BigDecimal("5.00"));
        assertThat(saved.getBalance()).isEqualByComparingTo("15.00");

        var txCaptor = ArgumentCaptor.forClass(Transaction.class);
        verify(txRepo).save(txCaptor.capture());
        assertThat(txCaptor.getValue().getType()).isEqualTo(TxType.DEPOSIT);
        assertThat(txCaptor.getValue().getAmount()).isEqualByComparingTo("5.00");

        verify(publisher).publishTransactionEvent("a1");
    }
}
