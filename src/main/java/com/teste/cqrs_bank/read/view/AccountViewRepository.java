package com.teste.cqrs_bank.read.view;

import org.springframework.data.mongodb.repository.MongoRepository;

public interface AccountViewRepository extends MongoRepository<AccountView, String> {}
