package com.teste.cqrs_bank.write.events;

import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Component;

/**
 * Publicador de eventos de domínio do Write Model para acionar a projeção (Read Model).
 * Atualmente publica {@link com.teste.cqrs_bank.write.events.TransactionEvent}.
 *
 * @since 1.0
 */
@Component
public class DomainEventPublisher {

    private final ApplicationEventPublisher publisher;

    public DomainEventPublisher(ApplicationEventPublisher publisher) {
        this.publisher = publisher;
    }

    public void publishTransactionEvent(String accountId) {
        publisher.publishEvent(new TransactionEvent(accountId));
    }
}
