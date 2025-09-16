package com.teste.cqrs_bank;

import org.junit.jupiter.api.BeforeAll;
import org.opentest4j.TestAbortedException;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.DockerClientFactory;
import org.testcontainers.containers.MongoDBContainer;
import org.testcontainers.containers.MySQLContainer;

@ActiveProfiles("test")
@SpringBootTest
public abstract class IntegrationTestBase {

    static final MySQLContainer<?> mysql = new MySQLContainer<>("mysql:8.0")
            .withDatabaseName("cqrs_bank")
            .withUsername("cqrs_user")
            .withPassword("123456");

    static final MongoDBContainer mongo = new MongoDBContainer("mongo:6.0");

    @BeforeAll
    static void start() {
        //Se Docker não está disponível, marca como SKIPPED em vez de falhar
        if (!DockerClientFactory.instance().isDockerAvailable()) {
            throw new TestAbortedException("Docker não disponível — pulando testes E2E");
        }
        mysql.start();
        mongo.start();
    }

    @DynamicPropertySource
    static void props(DynamicPropertyRegistry r) {
        r.add("spring.datasource.url", mysql::getJdbcUrl);
        r.add("spring.datasource.username", mysql::getUsername);
        r.add("spring.datasource.password", mysql::getPassword);
        r.add("spring.jpa.hibernate.ddl-auto", () -> "create");
        r.add("spring.data.mongodb.uri", mongo::getReplicaSetUrl);
        r.add("app.security.jwt.secret", () -> "test-secret-0123456789ABCDEFGHIJKLMNOPQRSTUVWXYZ");
        r.add("app.security.jwt.expiration-minutes", () -> "60");
    }
}
