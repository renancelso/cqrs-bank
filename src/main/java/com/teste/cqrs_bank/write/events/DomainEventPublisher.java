package com.teste.cqrs_bank.write.events;

import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Component;

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
