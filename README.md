# API de Gerenciamento de Pedidos

Uma aplicação backend RESTful para gerenciamento de usuários, produtos e
pedidos de clientes, desenvolvida com Java e Spring Boot.

Este projeto foi desenvolvido como um projeto de portfólio, com foco em modelagem de domínio, regras de negócio,
persistência de dados, design de APIs e testes automatizados.

### Principais Funcionalidades

-   Criação e consulta de pedidos
-   Listagem de pedidos por usuário
-   Adição de produtos aos pedidos
-   Atualização da quantidade de itens dos pedidos
-   Remoção de itens dos pedidos com preservação de registros históricos
-   Gerenciamento do ciclo de vida dos pedidos
-   Cancelamento de pedidos de acordo com as regras de negócio
-   Validação de requisições e tratamento centralizado de exceções
-   Persistência relacional e migrations versionadas do banco de dados
-   Testes automatizados do comportamento da aplicação

## Tecnologias Utilizadas

-   **Java 21**
-   **Spring Boot**
-   **Spring Web** 
-   **Spring Data JPA** 
-   **Hibernate** 
-   **PostgreSQL** 
-   **Flyway**
-   **Maven** 
-   **Docker Compose** 
-   **JUnit e Mockito**
-   **Testcontainers**

## Arquitetura

A aplicação segue uma arquitetura em camadas, organizada por domínio,
mantendo responsabilidades relacionadas agrupadas.

``` text
src/main/java/com/caio/ordermanagement/
├── user/
├── product/
└── order/
    ├── OrderController.java
    ├── OrderService.java
    ├── OrderRepository.java
    ├── dto/
    └── exception/
```

A estrutura de pacotes acima ilustra a organização principal por
domínio; os pacotes individuais contêm suas respectivas entidades,
repositories, services, DTOs e classes de suporte.

As principais responsabilidades arquiteturais são:

-   **Controllers:** expõem endpoints HTTP e recebem os dados das
    requisições.
-   **Services:** coordenam os casos de uso e aplicam regras de negócio
    no nível da aplicação.
-   **Repositories:** fornecem acesso aos dados por meio do Spring Data
    JPA.
-   **Entidades:** representam os objetos do domínio e encapsulam os
    comportamentos de negócio pertinentes.
-   **DTOs:** definem as estruturas de requisição e resposta expostas
    pela API.
-   **Migrations do Flyway:** versionam e gerenciam as alterações no
    esquema do banco de dados.

## Domínio de Pedidos

Os pedidos seguem um ciclo de vida definido:

``` text
CREATED
   ├──→ CONFIRMED
   └──→ CANCELLED

CONFIRMED
   ├──→ PROCESSING
   └──→ CANCELLED

PROCESSING
   ├──→ SHIPPED
   └──→ CANCELLED

SHIPPED
   └──→ DELIVERED
```

`DELIVERED` e `CANCELLED` são estados finais.

### Regras de Negócio

-   Cada pedido pertence a exatamente um usuário.
-   Apenas usuários ativos podem criar pedidos.
-   Um novo pedido começa no estado `CREATED`.
-   Um pedido deve conter pelo menos um item ativo.
-   Um produto não pode aparecer mais de uma vez no mesmo pedido.
-   As quantidades dos itens devem ser números inteiros positivos.
-   O preço unitário é registrado quando o item é adicionado ao pedido,
    preservando o preço daquele momento.
-   O total do pedido é calculado a partir dos subtotais dos itens
    ativos.
-   A remoção de um item o marca como inativo, em vez de excluir
    fisicamente seu registro.
-   Os itens só podem ser modificados enquanto o pedido estiver no
    estado `CREATED`.
-   Os pedidos só podem ser cancelados a partir dos estados permitidos.
-   Transições de estado inválidas são rejeitadas.
-   Pedidos e produtos não são excluídos fisicamente como parte de seu
    ciclo de vida normal.

## Endpoints da API

Os endpoints abaixo abrangem as operações implementadas de gerenciamento
de pedidos.

| Método | Endpoint | Descrição |
|---|---|---|
| `POST` | `/orders` | Criar um pedido |
| `GET` | `/orders/{id}` | Consultar um pedido por ID |
| `GET` | `/orders/user/{userId}` | Listar os pedidos de um usuário |
| `POST` | `/orders/{id}/items` | Adicionar um item ao pedido |
| `PATCH` | `/orders/{orderId}/items/{productId}` | Atualizar a quantidade de um item |
| `DELETE` | `/orders/{orderId}/items/{productId}` | Remover um item do pedido |
| `POST` | `/orders/{id}/confirm` | Confirmar um pedido |
| `POST` | `/orders/{id}/processing` | Iniciar o processamento de um pedido |
| `POST` | `/orders/{id}/ship` | Marcar um pedido como enviado |
| `POST` | `/orders/{id}/deliver` | Marcar um pedido como entregue |
| `POST` | `/orders/{id}/cancel` | Cancelar um pedido |

### Códigos de Status HTTP

A API utiliza códigos de status HTTP apropriados para as operações
implementadas, incluindo:

-   `200 OK` para consultas realizadas com sucesso
-   `201 Created` para criação de pedidos bem-sucedida
-   `204 No Content` para operações de alteração de estado bem-sucedidas
    que não retornam corpo na resposta
-   `400 Bad Request` para requisições inválidas e violações de regras
    de negócio
-   `404 Not Found` para recursos não encontrados

## Persistência de Dados

O PostgreSQL é utilizado como banco de dados relacional, com o Spring
Data JPA e o Hibernate responsáveis pela persistência objeto-relacional.

O Flyway gerencia as alterações do esquema do banco de dados por meio de
migrations SQL versionadas.

A validação do esquema pelo Hibernate é utilizada para identificar
inconsistências entre os mapeamentos das entidades e o esquema do banco
de dados.

O domínio preserva informações históricas relevantes, incluindo os
preços unitários dos itens dos pedidos e os itens removidos, em vez de
depender dos dados atuais dos produtos para reconstruir pedidos
anteriores.

## Testes

O projeto inclui testes automatizados para o comportamento da aplicação,
regras de negócio, validação de requisições e respostas HTTP.

A stack de testes inclui JUnit, Mockito, os recursos de teste do Spring
e a infraestrutura do Testcontainers.

Execute a suíte de testes com:

``` bash
mvn clean test
```

## Como Executar o Projeto

### Pré-requisitos

Certifique-se de que as seguintes ferramentas estejam instaladas:

-   Java 21
-   Maven
-   Docker e Docker Compose
-   Git

### 1. Clone o repositório

``` bash
git clone <https://github.com/cavinski/order-management>
cd order-management
```

### 2. Inicie o PostgreSQL

Na raiz do projeto, inicie o banco de dados utilizando a configuração
existente do Docker Compose:

``` bash
docker compose up -d
```

Certifique-se de que as credenciais do banco, o nome do banco de dados e
a porta estejam de acordo com a configuração da aplicação.

### 3. Execute a aplicação

``` bash
mvn spring-boot:run
```

A aplicação utiliza o arquivo `application.yaml` para suas
configurações. Verifique se as configurações necessárias para a conexão
com o banco de dados estão definidas antes de iniciar a aplicação.

### 4. Execute os testes

``` bash
mvn clean test
```

## Decisões de Engenharia

Diversas decisões foram tomadas neste projeto para priorizar a
consistência e a manutenibilidade:

-   **Regras de negócio no domínio:** operações inválidas são rejeitadas
    pelo modelo de domínio, em vez de depender exclusivamente dos
    controllers.
-   **Registro histórico de preços:** os itens preservam o preço
    unitário original, mesmo quando o preço atual do produto é alterado.
-   **Remoção lógica:** os itens removidos continuam disponíveis para
    consulta histórica.
-   **Transições de estado controladas:** as mudanças de status seguem
    um ciclo de vida explícito.
-   **Migrations de banco de dados:** a evolução do esquema é versionada
    e reproduzível.
-   **Arquitetura em camadas:** o tratamento HTTP, a lógica da aplicação
    e a persistência possuem responsabilidades distintas.
-   **Testes automatizados:** comportamentos importantes e cenários
    inválidos são verificados por meio de testes.

## Objetivos do Projeto

Este projeto demonstra a aplicação prática de conceitos de
desenvolvimento backend com Java, incluindo design de APIs REST,
persistência relacional, modelagem de domínio, validação, serviços de
aplicação com operações transacionais, migrations de banco de dados e
testes automatizados.

Ele também serve como base para explorar conceitos mais avançados de
engenharia backend em projetos futuros.
