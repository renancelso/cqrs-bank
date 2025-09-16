package com.teste.cqrs_bank.read;

import com.teste.cqrs_bank.domain.account.AccountRepository;
import com.teste.cqrs_bank.domain.transaction.Transaction;
import com.teste.cqrs_bank.domain.transaction.TransactionRepository;
import com.teste.cqrs_bank.read.view.AccountView;
import com.teste.cqrs_bank.read.view.AccountViewRepository;
import com.teste.cqrs_bank.write.events.TransactionEvent;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

import java.math.RoundingMode;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;
import java.util.Locale;

/**
 * Atualiza a projeção (Mongo) a partir dos eventos do Write Model.
 *
 * <ul>
 *   <li>Carrega a Account e as últimas transações; recalcula <b>SaldoTotal</b> como string.</li>
 *   <li>Monta <b>Historico</b> do mais recente para o mais antigo, convertendo:
 *       <code>DEPOSIT → "deposito"</code> e <code>BILL_PAYMENT → "saque"</code>.</li>
 *   <li>Formata <b>valor</b> como "0.00" e <b>data</b> como "dd-MM-yyyy HH:mm:ss".</li>
 *   <li>Atualiza/salva o documento <code>AccountView</code>.</li>
 * </ul>
 *
 * <p>Dispara via {@link org.springframework.context.event.EventListener}
 * e/ou {@link org.springframework.transaction.event.TransactionalEventListener}
 * em resposta a {@link com.teste.cqrs_bank.write.events.TransactionEvent}.</p>
 *
 * @since 1.0
 */
@Component
public class ProjectionUpdater {

    private static final DateTimeFormatter FMT = DateTimeFormatter.ofPattern("dd-MM-yyyy HH:mm:ss", new Locale("pt", "BR")).withZone(ZoneOffset.UTC);

    private final AccountRepository accountRepo;
    private final TransactionRepository txRepo;
    private final AccountViewRepository viewRepo;

    public ProjectionUpdater(AccountRepository accountRepo, TransactionRepository txRepo, AccountViewRepository viewRepo) {
        this.accountRepo = accountRepo;
        this.txRepo = txRepo;
        this.viewRepo = viewRepo;
    }

    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void on(TransactionEvent evt) {
        var acc = accountRepo.findById(evt.accountId()).orElse(null);
        if (acc == null) return;

        var txs = txRepo.findByAccountIdOrderByOccurredAtDesc(evt.accountId(), PageRequest.of(0, 100));

        var itens = txs.stream().map(this::toItem).toList();

        var view = AccountView.builder().id(acc.getId()).userId(acc.getUser().getId()).saldoTotal(acc.getBalance().setScale(2, RoundingMode.HALF_EVEN).toString()).historico(itens).updatedAt(java.time.Instant.now()).build();

        viewRepo.save(view); // upsert por _id
    }

    /**
     * Converte a {@link com.teste.cqrs_bank.domain.transaction.Transaction} em item do histórico
     * do Read Model, com rótulo de negócio ("deposito"/"saque"), valor "0.00" e data "dd-MM-yyyy HH:mm:ss".
     */
    private AccountView.HistoryItem toItem(Transaction tx) {
        String tipo = switch (tx.getType()) {
            case DEPOSIT -> "deposito";
            case BILL_PAYMENT -> "saque";
        };
        return AccountView.HistoryItem.builder().type(tipo).valor(tx.getAmount().setScale(2, RoundingMode.HALF_EVEN).toString()).data(FMT.format(tx.getOccurredAt().toInstant(ZoneOffset.UTC))).build();
    }
}
