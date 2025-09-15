# 🏦 CQRS Bank

Projeto de exemplo de **CQRS (Command Query Responsibility Segregation)** com **Spring Boot 3**, **MySQL 8** (modelo de escrita), **MongoDB 6** (modelo de leitura/projeção), **JWT + Spring Security** e **Swagger** para testar os endpoints.  
Os **testes de integração** usam **Testcontainers** (MySQL e Mongo reais).

---

## 📖 Sumário
- [Visão Geral](#visão-geral)
- [Arquitetura](#arquitetura)
- [Pré-requisitos](#pré-requisitos)
- [Configuração local](#configuração-local)
- [Rodando a aplicação](#rodando-a-aplicação)
- [Swagger / OpenAPI](#swagger--openapi)
- [Autenticação (JWT)](#autenticação-jwt)
- [Autenticação (JWT)](#autenticação-jwt)
- [Endpoints](#endpoints)
  - [Auth](#auth)
  - [Transações](#transações)
  - [Consulta (Read Model)](#consulta-read-model)
- [Fluxo de teste completo (via Swagger)](#fluxo-de-teste-completo-via-swagger)
- [Testes automatizados (Testcontainers)](#testes-automatizados-testcontainers)
- [Troubleshooting](#troubleshooting)
- [Notas de arquitetura](#notas-de-arquitetura)
- [Licença](#licença)

---

## Visão Geral
Este projeto simula um banco digital com operações de **cadastro, login, depósito, pagamento de contas e consulta de saldo/histórico**.  
A escrita (commands) é persistida em **MySQL**, e a leitura (queries) é servida por uma **projeção em MongoDB** atualizada a cada transação (listener/evento).

---

## Arquitetura
- **Spring Boot 3.5.x** (Web, Validation, Security, Data JPA, Data MongoDB, Actuator)
- **MySQL 8** → `users`, `accounts`, `transactions`
- **MongoDB 6** → `account_views` (projeção)
- **JWT (HS256)** + **Spring Security**
- **Swagger / springdoc-openapi** (UI e docs)
- **Testcontainers** (integração com MySQL/Mongo reais nos testes)

---

## Pré-requisitos
- **Java 21+**
- **Maven 3.9+**
- **MySQL 8** instalado localmente (para rodar a app)
- **MongoDB Community** instalado localmente (para rodar a app)
- **Docker Desktop** para **rodar os testes** (Testcontainers)  
  > Se não quiser rodar os testes, o Docker não é obrigatório para subir a aplicação.

---

## Configuração local

### 1) MySQL (modelo de escrita)
Crie o schema e o usuário (exemplo):
```sql
CREATE DATABASE IF NOT EXISTS cqrs_bank CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;

CREATE USER IF NOT EXISTS 'cqrs_user'@'%' IDENTIFIED BY '123456';
GRANT ALL PRIVILEGES ON cqrs_bank.* TO 'cqrs_user'@'%';
FLUSH PRIVILEGES;
```

### 2) MongoDB (modelo de leitura)
Crie o database (a collection é criada na primeira projeção):
```
Database: cqrs_bank_read
Collection: account_views
```

### 3) `application-dev.yml` (exemplo)
```yaml
spring:
  datasource:
    url: jdbc:mysql://localhost:3306/cqrs_bank?useSSL=false&allowPublicKeyRetrieval=true&serverTimezone=UTC
    username: cqrs_user
    password: 123456
  jpa:
    hibernate:
      ddl-auto: update
    properties:
      hibernate:
        format_sql: true
  data:
    mongodb:
      uri: mongodb://localhost:27017/cqrs_bank_read

server:
  port: 8080

app:
  security:
    jwt:
      secret: "change-me-please-32chars-minimum-ABCDEFGHIJKLMNOPQRSTUVWXYZ012345"
      expiration-minutes: 60
```

> **Importante**: a `secret` precisa ter **comprimento adequado** (32+ chars). Em produção, armazene como **variável de ambiente**.

---

## Rodando a aplicação
```bash
mvn spring-boot:run
```
- API: <http://localhost:8080>

---

## Swagger / OpenAPI
- UI: <http://localhost:8080/swagger-ui/index.html>
- JSON: <http://localhost:8080/v3/api-docs>

Se o botão **Authorize** não aparecer, garanta a dependência:
```xml
<dependency>
  <groupId>org.springdoc</groupId>
  <artifactId>springdoc-openapi-starter-webmvc-ui</artifactId>
  <version>2.6.0</version>
</dependency>
```
e a config de segurança do OpenAPI (ex. `OpenApiConfig`), definindo o **security scheme** `bearerAuth` (HTTP bearer / JWT).

---

## Autenticação (JWT)

Fluxo:
1. `POST /auth/signup` → cria usuário e já retorna um **token JWT**.
2. `POST /auth/login` → retorna **token JWT** para um usuário existente.
3. No Swagger, clique **Authorize** e cole o token (sem escrever `Bearer `).

> Endpoints de transação/consulta **exigem** o header `Authorization: Bearer <token>`.

---

## Endpoints

### Auth

#### `POST /auth/signup`
- Cadastra um novo usuário (**CPF único**, validado com `@CPF`).
- Body:
```json
{
  "fullName": "Renan Celso",
  "document": "39053344705",
  "login": "rcelso",
  "password": "123456"
}
```
- Resposta (exemplo):
```json
{ "token": "eyJhbGciOiJIUzI1NiIsInR5cCI6..." }
```

#### `POST /auth/login`
- Autentica um usuário e retorna **JWT**.
- Body:
```json
{ "login": "rcelso", "password": "123456" }
```

---

### Transações

> Todos os endpoints abaixo exigem **JWT** no header.

#### `POST /transactions/pay-bill`
- **Paga uma conta** (lança débito). Se não houver saldo, a conta fica **negativada**.
- Body:
```json
{ "amount": "150.00" }
```
- Resposta (exemplo):
```json
{ "balance": -150.00 }
```

#### `POST /transactions/deposit`
- **Deposita** dinheiro na conta do usuário logado.
- Regra: se o usuário está **negativado**, o depósito quita o **principal** e, **sobre a parte quitada**, cobra **juros de 1,02%** (o juro é debitado do próprio depósito, antes de creditar o restante).
- Body:
```json
{ "amount": "200.00" }
```
- Resposta (exemplo, após pagar dívida de 150 + 3 juros):
```json
{ "balance": 47.00 }
```

---

### Consulta (Read Model)

#### `GET /accounts/me/summary`
- Retorna o **saldo total** e o **histórico** (projeção do Mongo).
- Resposta (exemplo):
```json
{
  "SaldoTotal": "47.00",
  "Historico": [
    { "type": "deposito", "valor": "200.00", "data": "dd-MM-yyyy HH:mm:ss" },
    { "type": "saque",    "valor": "3.00", "data": "dd-MM-yyyy HH:mm:ss" },
    { "type": "saque",    "valor": "150.00", "data": "dd-MM-yyyy HH:mm:ss" }
  ]
}
```
> Label **"saque"** é usada para retiradas/pagamentos.  
> O histórico é ordenado da operação **mais recente** para a **mais antiga**.

---

## Fluxo de teste completo (via Swagger)

1. **Signup** → `POST /auth/signup`  
   ```json
   {
     "fullName": "Renan Celso",
     "document": "39053344705",
     "login": "rcelso",
     "password": "123456"
   }
   ```
   Copie o `token` da resposta.

2. **Authorize** → botão no topo da UI do Swagger.  
   Cole o JWT em **bearerAuth** (sem escrever "Bearer ").

3. **Pay Bill** → `POST /transactions/pay-bill`  
   ```json
   { "amount": "150.00" }
   ```
   Esperado: `balance = -150.00`.

4. **Deposit** → `POST /transactions/deposit`  
   ```json
   { "amount": "200.00" }
   ```
   Esperado: quita 150 + juros 3, sobra 47 → `balance = 47.00`.

5. **Summary** → `GET /accounts/me/summary`  
   Esperado: `SaldoTotal = "47.00"` e histórico com `"deposito"` e `"saque"`.

---

## Testes automatizados (Testcontainers)

- Os testes de integração usam **MySQL 8** e **Mongo 6** em containers **reais**.
- Pré-requisito: **Docker Desktop** em execução (WSL2 no Windows).  
- Rodar testes:
```bash
mvn test
```
- Dicas:
  - No primeiro run, as imagens `mysql:8.0` e `mongo:6.0` serão baixadas.
  - Use `mvn -q test` para logs mais enxutos.
  - Relatórios: `target/surefire-reports/`.

---

## Troubleshooting

- **Swagger sem botão Authorize**  
  - Falta o `springdoc-openapi-starter-webmvc-ui` ou a config `OpenApiConfig` com `bearerAuth`.

- **401 Unauthorized** nas rotas de transação/consulta  
  - Clique em **Authorize** no Swagger e cole o JWT válido.

- **Resumo vazio** após signup/login  
  - Execute ao menos **uma transação** (pay-bill/deposit) para gerar a **projeção** no Mongo.

- **Erro ao conectar MySQL/Mongo**  
  - Revise `application-dev.yml` (URL/usuário/senha).
  - Confirme que o MySQL e o Mongo **estão rodando** localmente.

- **Testcontainers falhou: "Could not find a valid Docker environment"**  
  - Abra o **Docker Desktop** e rode `docker version`.  
  - Tente novamente `mvn test`.

---

## Notas de arquitetura
- **CQRS**: MySQL é o **write model**; MongoDB é o **read model**.  
- **Projeção**: cada transação publica um **evento**; um **listener** recalcula e persiste o snapshot em `account_views`.  
- **Consistência eventual**: a projeção pode levar milissegundos entre a operação e a consulta.  
- **Regra de juros (1,02%)**: aplicada **somente** sobre a parte da dívida quitada pelo depósito; o juro **não entra** no saldo, é descontado do **próprio depósito**.

---

## Licença
MIT — uso livre para estudo e extensão do projeto.
