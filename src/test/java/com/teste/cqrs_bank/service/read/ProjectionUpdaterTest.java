package com.teste.cqrs_bank.read;

import com.teste.cqrs_bank.domain.account.Account;
import com.teste.cqrs_bank.domain.account.AccountRepository;
import com.teste.cqrs_bank.domain.transaction.Transaction;
import com.teste.cqrs_bank.domain.transaction.TransactionRepository;
import com.teste.cqrs_bank.domain.transaction.TxType;
import com.teste.cqrs_bank.read.view.AccountView;
import com.teste.cqrs_bank.read.view.AccountViewRepository;
import com.teste.cqrs_bank.write.events.TransactionEvent;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.data.domain.PageRequest;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

class ProjectionUpdaterTest {

    @Test
    void onEvent_upserta_view_e_mapeia_pagamento_para_saque() {
        var accountRepo = mock(AccountRepository.class);
        var txRepo = mock(TransactionRepository.class);
        var viewRepo = mock(AccountViewRepository.class);

        var acc = Account.builder().id("a1").balance(new BigDecimal("47.00")).build();
        acc.setUser(new com.teste.cqrs_bank.domain.user.User()); // só para evitar NPE
        acc.getUser().setId("u1");

        when(accountRepo.findById("a1")).thenReturn(Optional.of(acc));
        when(txRepo.findByAccountIdOrderByOccurredAtDesc(eq("a1"), any(PageRequest.class)))
                .thenReturn(List.of(
                        Transaction.builder().type(TxType.DEPOSIT).amount(new BigDecimal("47.00"))
                                .occurredAt(LocalDateTime.now()).build(),
                        Transaction.builder().type(TxType.BILL_PAYMENT).amount(new BigDecimal("153.00"))
                                .occurredAt(LocalDateTime.now()).build()
                ));

        var updater = new ProjectionUpdater(accountRepo, txRepo, viewRepo);
        updater.on(new TransactionEvent("a1"));

        var captor = ArgumentCaptor.forClass(AccountView.class);
        verify(viewRepo).save(captor.capture());
        var view = captor.getValue();

        assertThat(view.getId()).isEqualTo("a1");
        assertThat(view.getSaldoTotal()).isEqualTo("47.00");
        assertThat(view.getHistorico()).hasSize(2);
        assertThat(view.getHistorico().get(0).getType()).isIn("deposito", "saque");
        assertThat(view.getHistorico().stream().anyMatch(h -> "saque".equals(h.getType()))).isTrue();
        assertThat(view.getHistorico().stream().anyMatch(h -> "deposito".equals(h.getType()))).isTrue();
    }
}
