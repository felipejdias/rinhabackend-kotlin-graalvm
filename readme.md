# Rinha de Backend 2024/Q1 — Kotlin + Spring Boot + GraalVM Native

Implementação da [Rinha de Backend 2024/Q1](https://github.com/zanfranceschi/rinha-de-backend-2024-q1)
(crébitos) em Kotlin, compilada como binário nativo com GraalVM.

## Stack

| Componente | Versão |
|---|---|
| Java | 25 (toolchain do Gradle) |
| Kotlin | 2.3.21 |
| Spring Boot | 4.1.0 (Spring MVC + JDBC/Hikari, virtual threads) |
| GraalVM (build nativo) | `container-registry.oracle.com/graalvm/native-image:25` |
| Runtime da API | `container-registry.oracle.com/os/oraclelinux:10-slim` (só o binário nativo, sem JVM) |
| Banco | `postgres:18-alpine` |
| Load balancer | `nginx:1.30-alpine` (round robin, porta 9999) |
| Teste de carga | Gatling 3.15.1 (módulo `teste-gatling`) |
| Gradle | 9.5.1 (wrapper) |

## Pré-requisitos

- JDK 25 (para build local; o Gradle resolve via toolchain).
- Docker Desktop com **Settings > Resources > Network > Enable host networking DESLIGADO**
  (padrão). Veja [por quê](#por-que-deixar-o-enable-host-networking-desligado).
- Portas livres na VM do Docker: `9999` (nginx), `8081`/`8082` (api01/api02), `5432` (postgres).

## Passo a passo para testar

Todos os comandos a partir da raiz do projeto, no PowerShell:

```powershell
# 1. Build da imagem nativa (~4-5 min)
docker compose -f rinha-backend-kotlin/docker-compose.yml build api01

# 2. Subir o ambiente (o banco é recriado do zero a cada "down/up", sem volume)
docker compose -f rinha-backend-kotlin/docker-compose.yml up -d

# 3. Rodar o teste de carga (Gatling em container com --network host, ~5 min)
.\scripts\run-load-test.ps1

# 4. Abrir o relatório (pasta mais recente)
$report = Get-ChildItem teste-gatling\build\reports\gatling -Directory | Sort-Object LastWriteTime | Select-Object -Last 1
Start-Process "$($report.FullName)\index.html"

# 5. Derrubar o ambiente
docker compose -f rinha-backend-kotlin/docker-compose.yml down
```

> **Importante:** a simulação valida que os clientes começam com saldo 0. Para rodar o teste
> de novo, faça `down` + `up -d` antes, para recriar o banco.

Para acompanhar o consumo dos containers durante o teste, em outro terminal:

```powershell
docker stats nginx postgress rinha-backend-kotlin-api01-1 rinha-backend-kotlin-api02-1
```

### Opções do script de carga

```powershell
.\scripts\run-load-test.ps1 -BaseUrl "http://localhost:9999" -Simulation RinhaBackendCrebitosSimulation
```

`.\gradlew :teste-gatling:gatlingRun` direto no Windows não alcança a API (a rede host fica dentro
da VM do Docker); use sempre o script.

## Rede: `network_mode: host` (sem bridge)

Todos os serviços rodam com `network_mode: host`. No Windows, o proxy de publicação de portas do
Docker Desktop (WSL2/vpnkit) engasga sob alta concorrência e derruba requisições sem nenhum erro
na aplicação (mesmo problema investigado no projeto `teste-itau`). Com host networking não há
NAT/proxy entre nginx, APIs e banco, e o Gatling rodando em container com `--network host`
também não passa pelo proxy.

- As APIs não têm nome de serviço: ficam em `127.0.0.1:8081` e `127.0.0.1:8082` (ver `config/nginx.conf`).
- A API lê o host do banco da variável `DB_HOSTNAME` (padrão `localhost`).

### Por que deixar o "Enable host networking" desligado

`network_mode: host` funciona sem essa opção: os containers compartilham a rede da VM do Docker
Desktop e se enxergam por `localhost`. A opção serve apenas para **espelhar no Windows** as portas
abertas na VM, e faz isso colocando um proxy do Docker Desktop no loopback da VM. Isso reintroduz
justamente a camada de proxy que o host networking deveria evitar. Medido neste projeto:

| Enable host networking | Resultado do Gatling (61.503 requisições) |
|---|---|
| Desligado | **0 erros**, p99 3ms |
| Ligado | **~48% `Connection refused`** (não chegam ao nginx), com `localhost` ou `127.0.0.1` |

Ligado, também quebra o Gradle dentro de um container `--network host` ("Could not connect to the
Gradle daemon"), porque cliente, daemon e workers conversam por portas no loopback. Por isso o
`run-load-test.ps1` compila a simulação numa rede comum e só roda a JVM do Gatling na rede host.

O preço de deixar desligado: o Windows não alcança `localhost:9999`. Para chamar a API, use um
container na rede host (ver [Chamar a aplicação](#chamar-a-aplicação)).

## Recursos (limite da Rinha: 1.5 CPU / 550MB)

| Serviço | CPU | Memória | Pico medido no teste |
|---|---|---|---|
| api01 | 0.4 | 100MB | ~10% CPU / 32MB |
| api02 | 0.4 | 100MB | ~10% CPU / 32MB |
| nginx | 0.2 | 20MB | ~9% CPU / 6MB |
| db | 0.5 | 330MB | ~14% CPU / 97MB |
| **Total** | **1.5** | **550MB** | |

Tuning aplicado depois de monitorar o primeiro teste (990 erros, p99 4,5s → 0 erros, p99 3ms):

- **nginx:** `worker_connections 4096` (1000 estourava no pico; cada requisição usa 2 conexões),
  `keepalive` com as APIs e `worker_processes 1` (`auto` enxergaria todas as CPUs da VM via host network).
- **Hikari:** pool de 15 conexões por API (via `SPRING_DATASOURCE_HIKARI_*` no compose). Com 2x50 o
  postgres ficava no limite de memória sem ganho, já que o banco tem só 0.5 CPU.
- **CPU:** as APIs nativas ficavam ociosas; CPU movida para o banco, que era o gargalo.

## Resultados do teste de carga (simulação oficial da Rinha)

Execução de 2026-10-08, com o tuning acima e "Enable host networking" desligado:
**61.503 requisições, 0 falhas, 100% abaixo de 800ms**. Carga de pico: ~340 req/s
(220 débitos + 110 créditos + 10 extratos por segundo).

| Endpoint | Requisições | p50 | p95 | p99 | Máx |
|---|---|---|---|---|---|
| Débitos (`POST /clientes/{id}/transacoes`) | 39.660 | 1ms | 1ms | 2ms | 23ms |
| Créditos (`POST /clientes/{id}/transacoes`) | 19.860 | 1ms | 1ms | 2ms | 17ms |
| Extratos (`GET /clientes/{id}/extrato`) | 1.860 | 2ms | 3ms | 8ms | 10ms |
| Validações | 123 | 7ms | 165ms | 166ms | 166ms |

As validações concentram o máximo porque rodam no início do teste, com 25 requisições simultâneas
no mesmo cliente (teste de concorrência) enquanto o pool de conexões ainda está aquecendo.

Evolução ao longo do tuning:

| | Antes do tuning | Depois do tuning | Host networking ligado |
|---|---|---|---|
| Falhas | 990 (1,6%) | **0** | 29.729 (48%, `Connection refused`) |
| Média | 145ms | **1ms** | 4ms |
| p99 | 4.536ms | **3ms** | 13ms |
| Máximo | 7.775ms | **167ms** | 147ms |

### Capacidade estimada com os recursos atuais

Estimativa a partir do custo de CPU por requisição medido no pico (CPU usada ÷ req/s), sem teste dedicado:

| Serviço | Custo/req | Limite de CPU | Teto estimado |
|---|---|---|---|
| APIs (2) | ~0,56ms | 0,8 | ~1.400 req/s |
| Postgres | ~0,42ms | 0,5 | ~1.200 req/s |
| **nginx** | ~0,26ms | 0,2 | **~750 req/s** (gargalo) |

Teto de ~750 req/s, sustentável com latência baixa até ~550-600 req/s. Redistribuindo a CPU na
proporção do custo (APIs 0,34+0,34, nginx 0,31, banco 0,51), o teto sobe para ~1.200 req/s.
Memória, pool do Hikari, conexões do nginx e lock por cliente não são limitantes nessa faixa.

## Build

```powershell
# build JVM + compilação da simulação Gatling
.\gradlew build gatlingClasses

# imagem nativa sem compose (contexto = raiz do projeto)
docker build -f rinha-backend-kotlin/Dockerfile -t rinha-backend-2024-q1 .
```

O `native-image` está limitado a 4GB de heap e 4 threads (`build.gradle.kts`); sem isso ele usa
~75% da RAM e todas as CPUs da VM do Docker Desktop e derruba o daemon no meio do build.

## Chamar a aplicação

Como o Windows não enxerga a rede host da VM, as chamadas são feitas de um container na rede host:

### Consultar extrato
```powershell
docker run --rm --network host curlimages/curl:8.11.1 -s http://localhost:9999/clientes/1/extrato
```

### Criar uma transação
```powershell
docker run --rm --network host curlimages/curl:8.11.1 -s -X POST http://localhost:9999/clientes/1/transacoes -H "Content-Type: application/json" -d '{\"valor\": 1000, \"tipo\": \"d\", \"descricao\": \"descricao\"}'
```

## Regras da Rinha atendidas

- `POST /clientes/{id}/transacoes`: 200 com `limite`/`saldo`; 422 se o débito ultrapassar o limite
  ou se o payload for inválido (`tipo` ≠ `c`/`d`, `descricao` vazia/nula/> 10 caracteres, `valor` não inteiro).
- `GET /clientes/{id}/extrato`: saldo, `data_extrato`, `limite` e até 10 últimas transações (`realizada_em`) em ordem decrescente.
- Cliente inexistente (ex.: id 6): 404 nos dois endpoints.
- 5 clientes pré-cadastrados em `rinha-backend-kotlin/scripts/script.sql`.
- Para submeter na Rinha, a imagem precisa estar num registry público (ex.: Docker Hub) e o
  `docker-compose.yml` deve referenciá-la em vez de `rinha-backend-2024-q1:latest` local.
