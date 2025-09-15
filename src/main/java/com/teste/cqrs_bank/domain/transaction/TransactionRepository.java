package com.teste.cqrs_bank.domain.transaction;

import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface TransactionRepository extends JpaRepository<Transaction, String> {

    List<Transaction> findByAccountIdOrderByOccurredAtDesc(String accountId);

    List<Transaction> findByAccountIdOrderByOccurredAtDesc(String accountId, Pageable pageable);
}
