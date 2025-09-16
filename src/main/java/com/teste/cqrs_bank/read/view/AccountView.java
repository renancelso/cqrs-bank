package com.teste.cqrs_bank.read.view;

import lombok.*;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.Instant;
import java.util.List;

/**
 * Documento Mongo do snapshot pronto para UI (collection: account_views).
 * Chaves e formato:
 * {
 * "SaldoTotal": "0.00",
 * "Historico": [
 * { "type": "deposito"|"saque", "valor": "0.00", "data": "dd-MM-yyyy HH:mm:ss" }
 * ]
 * }
 *
 * <p>Os valores ficam como <b>strings formatadas</b>; cálculo numérico permanece no Write Model.</p>
 *
 * @since 1.0
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Document(collection = "account_views")
public class AccountView {

    @Id
    private String id;
    private String userId;
    private String saldoTotal;
    private List<HistoryItem> historico;
    private Instant updatedAt;

    @Getter
    @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class HistoryItem {
        private String type;
        private String valor;
        private String data;
    }
}
