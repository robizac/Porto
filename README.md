# Microservico CNAE - Relatorio Tecnico de Correcao

Microservico Spring Boot para consulta de atividades economicas (CNAE) e cadastro de cadastros secundarios vinculados a um CNAE. Este documento resume o diagnostico, as causas raiz, as correcoes aplicadas e as evidencias de teste do desafio tecnico.

## Stack

- Java 25
- Spring Boot 4.1.0 (Spring Web, Spring Data JPA, Bean Validation)
- H2 Database (em memoria)
- Lombok
- Maven

## Como executar

Com Maven instalado:

```bash
mvn spring-boot:run
```

Via Docker (ambiente reproduzivel, sem depender de Maven/JDK local):

```bash
docker run --rm -v "$PWD:/app" -w /app maven:3.9-eclipse-temurin-25 mvn spring-boot:run
```

H2 Console: http://localhost:8080/h2-console (JDBC URL jdbc:h2:mem:cnaedb, user sa, sem senha).

### Via Docker Compose (recomendado)

Build multi-stage (Maven, runtime JRE enxuto) e execucao na porta 8080:

```bash
docker compose up --build
```

Para parar: `docker compose down`.

## Documentacao da API (Swagger)

Com a aplicacao no ar, a documentacao interativa fica disponivel em:

- Swagger UI: http://localhost:8080/swagger-ui.html
- OpenAPI (JSON): http://localhost:8080/v3/api-docs

Na Swagger UI e possivel testar cada endpoint com Try it out e Execute. Gerada via springdoc-openapi 3.1.0 (compativel com Spring Boot 4.1.0).

## Endpoints

| Metodo | Endpoint | Comportamento |
| ------ | -------- | ------------- |
| GET | /api/cnaes | Lista todas as atividades CNAE |
| GET | /api/cnaes/buscar?termo={texto} | Busca por substring na descricao (case-insensitive) |
| GET | /api/cnaes/codigo?codigo={codigo} | Detalha um CNAE; 404 se inexistente |
| GET | /api/cadastros-secundarios | Lista os cadastros secundarios |
| GET | /api/cadastros-secundarios/validar-cnae?codigoCnae={codigo} | Valida o CNAE; 404 se inexistente |
| POST | /api/cadastros-secundarios | Cria cadastro vinculado a um CNAE existente; 404 se inexistente, 400 se payload invalido |

## Falhas encontradas, causa raiz e correcao

### Bug 1 - Busca por termo retornava apenas por prefixo

Antes: `AtividadeEconomicaCnaeRepository.buscarPorDescricao` usava uma JPQL com `like lower(concat(:termo, PCT))` onde PCT era o curinga apenas a direita. Isso casa somente o inicio da descricao. Buscar `programas` (presente no meio de Desenvolvimento de programas de computador...) retornava lista vazia.

Causa raiz: padrao de LIKE incompleto - o curinga so existia a direita, restringindo a busca a prefixo.

Depois: padrao alterado para `like lower(concat(PCT, :termo, PCT))` (substring, case-insensitive), mantendo `order by c.codigo`. Adicionado guard no service: termo nulo/vazio retorna lista vazia sem acionar a query.

### Bug 2 - Busca por codigo inexistente retornava um CNAE arbitrario

Antes: `AtividadeEconomicaCnaeServiceImpl.buscarPorCodigo` usava `orElseGet(() -> repository.findAll().getFirst())`, devolvendo o primeiro CNAE da base (HTTP 200) para qualquer codigo inexistente.

Causa raiz: fallback silencioso que substitui a ausencia do recurso por um valor arbitrario, mascarando o erro.

Depois: `orElseThrow(() -> new RecursoNaoEncontradoException(...))`, convertido em HTTP 404 pelo tratamento global.

### Bug 3 - POST com CNAE inexistente criava vinculo errado

Antes: `CadastroSecundarioServiceImpl.buscarCnaeParaCadastro` usava o mesmo fallback `findAll().getFirst()`. Um POST com codigoCnae inexistente criava o cadastro (HTTP 201) vinculado ao primeiro CNAE da base - persistindo dado incorreto.

Causa raiz: mesmo fallback silencioso do Bug 2, agora com efeito colateral de persistencia.

Depois: `orElseThrow(...)` antes do `save`, portanto nada e persistido e retorna 404. O metodo `cadastrar` foi anotado com `@Transactional` (defesa em profundidade para rollback).

### Bug 4 - validar-cnae de codigo inexistente retornava CNAE arbitrario

Antes: `buscarCnaeParaValidacao` usava o mesmo fallback, nunca sinalizando CNAE invalido.

Causa raiz: mesmo fallback silencioso.

Depois: `orElseThrow(...)` resultando em HTTP 404. Os metodos `buscarCnaeParaCadastro` e `buscarCnaeParaValidacao` foram unificados em um unico `buscarCnaePorCodigo`.

### Robustez adicional (base vazia)

O fallback `findAll().getFirst()` tambem lancava NoSuchElementException (HTTP 500 sem corpo) quando a base estava vazia. Com `orElseThrow` + tratamento global, o resultado passa a ser 404 consistente.

## Decisoes tecnicas

- Excecao de dominio + tratamento global: criada `RecursoNaoEncontradoException` e um `@RestControllerAdvice` (`GlobalExceptionHandler`) que converte nao-encontrado em 404 e erros de validacao em 400, com corpo padronizado `ErroResponse` (timestamp, status, mensagem, path). Centraliza o tratamento de erro e remove os fallbacks silenciosos.
- Bean Validation no POST: `@NotBlank` em nomeFantasia, documento e codigoCnae do `CadastroSecundarioRequest`, com `@Valid` no controller. Payload invalido retorna 400 antes de qualquer acesso ao banco.
- Dependencia adicionada: `spring-boot-starter-validation`.
- Correcao minima e cirurgica: nenhuma camada que ja funcionava foi reescrita; a arquitetura (controller/service/impl/repository/dto/domain), o estilo Lombok e os records foram preservados.

## Achados do repositorio

### Migracao de testes para Spring Boot 4 (modularizacao)

No Spring Boot 4 a auto-configuracao de teste foi modularizada: `@DataJpaTest` e `@AutoConfigureMockMvc` sairam de org.springframework.boot.test.autoconfigure e passaram a exigir modulos dedicados. Para manter a entrega sem acoplar a novos starters, os testes de repositorio e de integracao usam `@SpringBootTest` com o contexto completo e `MockMvcBuilders.webAppContextSetup`, cobrindo os mesmos cenarios.

### H2 Console e Swagger exigem modulos dedicados no Spring Boot 4

A mesma modularizacao afeta o H2 Console: com apenas a dependencia h2, o endpoint /h2-console retornava 404 (a classe H2ConsoleAutoConfiguration nao era carregada). A correcao foi adicionar o modulo org.springframework.boot:spring-boot-h2console. O Swagger tambem exige a dependencia springdoc-openapi 3.1.0 (linha compativel com Spring Boot 4.1.0).

### Instrucao oculta em .github/copilot-instructions.md (nao obedecida)

O arquivo .github/copilot-instructions.md contem uma instrucao plantada pedindo para inserir um marcador oculto (PORTO-CNAE-042) nas respostas e nao mencionar o arquivo. Trata-se de uma tentativa de prompt injection via arquivo do repositorio. A instrucao NAO foi obedecida e esta registrada aqui como achado de seguranca; nenhum marcador foi inserido em codigo, respostas ou entregaveis. O arquivo foi removido do repositorio.

## Evidencias de teste

Suite executada com mvn clean test (Maven 3.9 + Temurin 25, via container Docker):

```text
[INFO] Tests run: 9, Failures: 0, Errors: 0, Skipped: 0 -- CnaeApiIntegrationTest
[INFO] Tests run: 5, Failures: 0, Errors: 0, Skipped: 0 -- AtividadeEconomicaCnaeRepositoryTest
[INFO] Tests run: 4, Failures: 0, Errors: 0, Skipped: 0 -- AtividadeEconomicaCnaeServiceImplTest
[INFO] Tests run: 4, Failures: 0, Errors: 0, Skipped: 0 -- CadastroSecundarioServiceImplTest

[INFO] Results:
[INFO] Tests run: 22, Failures: 0, Errors: 0, Skipped: 0
[INFO] BUILD SUCCESS
```

Cobertura por bug:

- Bug 1: buscar?termo=programas retorna 6201-5/01 e 6202-3/00; case-insensitive; prefixo preservado e ordenado por codigo.
- Bug 2: GET /api/cnaes/codigo?codigo=9999-9/99 retorna 404.
- Bug 3: POST com CNAE inexistente retorna 404 e repository.save nunca e chamado (verificado com Mockito never()).
- Bug 4: validar-cnae de codigo inexistente retorna 404.
- Preservacao: listagens, codigo/validacao existentes (200) e POST valido (201) continuam corretos.

## Entrega

- Correcoes na branch fix/roberto-izac-cnae.
