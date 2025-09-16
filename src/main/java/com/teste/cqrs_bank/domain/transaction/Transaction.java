package com.teste.cqrs_bank.domain.transaction;

import com.teste.cqrs_bank.domain.account.Account;
import jakarta.persistence.*;
import jakarta.validation.constraints.Positive;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

/**
 * Lançamento financeiro atômico no Write Model.
 * Campos principais: tipo (DEPOSIT/BILL_PAYMENT), valor (Decimal, scale=2), occurredAt (timestamp).
 *
 * <p>O histórico “pronto para UI” é montado no Read Model (Mongo) a partir dos eventos.</p>
 * @since 1.0
 */
@Getter @Setter
@NoArgsConstructor @AllArgsConstructor @Builder
@EqualsAndHashCode(of = "id")
@Entity
@Table(
        name = "transactions",
        indexes = {
                @Index(name = "idx_transactions_account_date", columnList = "account_id, occurred_at")
        }
)
public class Transaction {

    @Id
    @Column(name = "id", length = 36, columnDefinition = "char(36)")
    private String id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "account_id", nullable = false,
            foreignKey = @ForeignKey(name = "fk_transactions_account"))
    private Account account;

    @Enumerated(EnumType.STRING)
    @Column(name = "type", nullable = false, length = 20)
    private TxType type;

    @Positive
    @Column(name = "amount", nullable = false, precision = 19, scale = 2)
    private BigDecimal amount;

    @CreationTimestamp
    @Column(name = "occurred_at", nullable = false, columnDefinition = "datetime(6)")
    private LocalDateTime occurredAt;

    @PrePersist
    void prePersist() {
        if (this.id == null) this.id = UUID.randomUUID().toString();
        if (this.amount != null) this.amount = this.amount.setScale(2);
    }
}
