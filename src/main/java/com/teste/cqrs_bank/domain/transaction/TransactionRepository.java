package com.teste.cqrs_bank.domain.transaction;

import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

/**
 * Repositório JPA de Transaction.
 * Exposição de consultas ordenadas por data (desc) com paginação para projeções/relatórios.
 *
 * @since 1.0
 */
public interface TransactionRepository extends JpaRepository<Transaction, String> {

    List<Transaction> findByAccountIdOrderByOccurredAtDesc(String accountId);

    List<Transaction> findByAccountIdOrderByOccurredAtDesc(String accountId, Pageable pageable);
}
