package com.teste.cqrs_bank.write.events;

/**
 * Evento de domínio emitido após mutações no Write Model para atualização da projeção.
 * Carrega o <code>accountId</code> que deve ser (re)processado no Read Model.
 *
 * @since 1.0
 */
public record TransactionEvent(String accountId) {
}
