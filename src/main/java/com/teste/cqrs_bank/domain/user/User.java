package com.teste.cqrs_bank.domain.user;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import java.time.LocalDateTime;
import java.util.UUID;

@Getter @Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@EqualsAndHashCode(of = "id")
@Entity
@Table(
        name = "users",
        uniqueConstraints = {
                @UniqueConstraint(name = "uq_users_login", columnNames = "login"),
                @UniqueConstraint(name = "uq_users_document", columnNames = "document") // ✅
        }
)
public class User {

    @Id
    @Column(name = "id", length = 36, columnDefinition = "char(36)")
    private String id;

    @Column(name = "full_name", nullable = false, length = 150)
    private String fullName;

    @Column(name = "document", nullable = false, length = 32)
    private String document;

    @Column(name = "login", nullable = false, length = 64)
    private String login;

    @Column(name = "password_hash", nullable = false, length = 100)
    private String passwordHash;

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, columnDefinition = "datetime(6)")
    private LocalDateTime createdAt;

    @PrePersist
    void prePersist() {
        if (this.id == null) this.id = UUID.randomUUID().toString();
    }
}