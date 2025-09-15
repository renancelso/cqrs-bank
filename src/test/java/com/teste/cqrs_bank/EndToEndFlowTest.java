package com.teste.cqrs_bank;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.awaitility.Awaitility;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.time.Duration;

import static org.hamcrest.Matchers.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@AutoConfigureMockMvc
class EndToEndFlowTest extends IntegrationTestBase {

    @Autowired MockMvc mvc;
    @Autowired ObjectMapper om;

    @Test
    void fluxoCompleto_cobra102_porcento_e_projeta_no_mongo() throws Exception {
        // 1) Signup
        String signup = """
            {
              "fullName": "Renan Celso",
              "document": "39053344705",
              "login": "rcelso",
              "password": "123456"
            }
            """;
        mvc.perform(post("/auth/signup")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(signup))
                .andExpect(status().isOk());

        // 2) Login
        String loginJson = "{ \"login\":\"rcelso\", \"password\":\"123456\" }";
        String loginResp = mvc.perform(post("/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(loginJson))
                .andExpect(status().isOk())
                .andReturn().getResponse().getContentAsString();
        JsonNode root = om.readTree(loginResp);
        String token = root.get("token").asText();

        // 3) Pagar conta 150 (negativa)
        mvc.perform(post("/transactions/pay-bill")
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"amount\":\"150.00\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.balance", is(-150.00)));

        // 4) Depositar 200 (paga 150 + juros 3 = 153; sobra 47)
        mvc.perform(post("/transactions/deposit")
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"amount\":\"200.00\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.balance", is(47.00)));

        // 5) Summary — aguardar a projeção cair no Mongo (eventual consistency)
        Awaitility.await().atMost(Duration.ofSeconds(5)).pollInterval(Duration.ofMillis(200))
                .untilAsserted(() ->
                        mvc.perform(get("/accounts/me/summary")
                                        .header("Authorization", "Bearer " + token))
                                .andExpect(status().isOk())
                                .andExpect(jsonPath("$.SaldoTotal", is("47.00")))
                                .andExpect(jsonPath("$.Historico", isA(java.util.List.class)))
                                .andExpect(jsonPath("$.Historico[*].type", hasItems("deposito", "saque")))
                );
    }
}
