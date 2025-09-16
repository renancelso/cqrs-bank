package com.teste.cqrs_bank.read.view;

import org.springframework.data.mongodb.repository.MongoRepository;

/**
 * Repositório Mongo de AccountView. @since 1.0
 */
public interface AccountViewRepository extends MongoRepository<AccountView, String> {
}
