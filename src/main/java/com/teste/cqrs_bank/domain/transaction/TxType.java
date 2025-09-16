package com.teste.cqrs_bank.domain.transaction;

/**
 * Tipos de transação suportados:
 * - DEPOSIT (depósito/crédito)
 * - BILL_PAYMENT (pagamento/débito)
 *
 * @since 1.0
 */
public enum TxType {
    DEPOSIT,
    BILL_PAYMENT
}
