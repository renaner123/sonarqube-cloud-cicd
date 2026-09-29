# Task Manager: SonarQube com GitHub Actions

Projeto didático para demonstrar análise de qualidade de código com SonarQube Cloud e GitHub Actions. O exemplo contém um backend Java/Spring Boot e um frontend Vue 3, com problemas intencionais marcados por `SONAR-DEMO`.

O caminho principal deste repositório é o workflow [`.github/workflows/sonarqube.yml`](.github/workflows/sonarqube.yml): ele executa build, testes, cobertura e análise do backend e do frontend em jobs paralelos. Os jobs rodam em `push` para `main`, em pull requests direcionados a `main` e manualmente pelo GitHub Actions.

## Como funciona o workflow

### Backend: Java, Maven e JaCoCo

O job configura o JDK 21, executa `mvn clean verify` para compilar, testar e gerar o relatório JaCoCo, e depois envia a análise ao SonarQube Cloud. O `pom.xml` mantém o alvo de compilação em Java 17; o JDK do runner não altera esse alvo. O parâmetro `sonar.qualitygate.wait=true` faz o job aguardar o resultado do Quality Gate e falhar quando ele reprova.

### Frontend: Vue, Node.js e Vitest

O job configura Node.js 20, instala dependências com `npm ci`, executa `npm run test:coverage` e envia a análise do frontend. **Na configuração atual, esse job não espera o resultado do Quality Gate do frontend.** Assim, o check do frontend valida a execução dos testes e do scanner, mas uma reprovação do gate do frontend não necessariamente faz o job falhar.

## Configuração no GitHub e SonarQube Cloud

1. Importe o repositório no SonarQube Cloud e configure os projetos de análise para backend e frontend, conforme a organização do monorepo.
2. Adicione os secrets do repositório em **Settings → Secrets and variables → Actions**:

   | Secret | Valor |
   |---|---|
   | `SONAR_TOKEN` | Token de análise do SonarQube Cloud |
   | `SONAR_HOST_URL` | `https://sonarcloud.io` |

3. Confira no workflow se os `projectKey` e caminhos dos projetos correspondem aos seus projetos no SonarQube Cloud.
4. Execute o workflow pelo menos uma vez para que os checks apareçam nas configurações do repositório.
5. Em **Settings → Rules → Rulesets**, crie ou atualize um ruleset ativo para `main`, exija pull requests e selecione como obrigatórios os checks do workflow que devem passar antes do merge.

> Para bloquear merges por reprovação do Quality Gate, o job que realiza cada análise precisa reportar a falha como check obrigatório. No workflow atual, isso está configurado para o backend, mas não para o frontend. Além disso, verifique as limitações vigentes do SonarQube Cloud para bloqueio de PRs em projetos de monorepo antes de depender desse fluxo em outro repositório.

## Executar a aplicação localmente (opcional)

O Docker Compose continua disponível para subir o MySQL e uma instância local do SonarQube Community:

```bash
docker compose up -d mysql sonarqube
```

A interface do SonarQube fica em [http://localhost:9000](http://localhost:9000). Na primeira inicialização, entre com `admin` / `admin` e troque a senha. Esse ambiente é uma alternativa local para explorar a ferramenta; não é necessário para executar o workflow do GitHub Actions, que analisa no SonarQube Cloud.

Para encerrar os serviços:

```bash
docker compose down
```

Para também apagar os volumes de dados locais:

```bash
docker compose down -v
```

O Compose inclui ainda um serviço `sonar-scanner` opcional, no profile `scan`, voltado à análise local do frontend. Seu uso depende de um `SONAR_TOKEN` válido e da configuração local do projeto. Ele não substitui o workflow descrito acima.

## Executar a aplicação sem Docker (opcional)

Backend:

```bash
cd backend
mvn spring-boot:run
```

Frontend, em outro terminal:

```bash
cd frontend
npm ci
npm run dev
```

## Problemas intencionais

| Arquivo | Exemplo demonstrado | Tag |
|---|---|---|
| `AuthService.java` | Log de senha em texto claro | `log de senha` |
| `AuthService.java` | Comparação de String com `==` | `comparação com ==` |
| `UserService.java` | `Optional.get()` sem verificar presença | `Optional.get() sem verificar` |
| `TaskRepository.java` | Query com concatenação de String | `concatenação em query` |
| `TaskService.java` | Método longo com muitas responsabilidades | `método longo` |
| `TaskService.java` | Número mágico sem constante nomeada | `número mágico` |
| `TaskService.java` | Lógica de prioridade duplicada | `lógica duplicada` |
| `DashboardService.java` | Variáveis com nomes sem significado | `variáveis sem nome` |
| `TaskCommentService.java` | Bloco `catch` genérico ignorado silenciosamente | `catch genérico ignorado` |
| Serviços e controllers | Classes sem testes | `sem testes` |
| `TasksView.vue` | Componente com mais de 200 linhas | `múltiplas responsabilidades` |
| `TasksView.vue` | `formatDate` duplicada, já existente em `useDate.js` | `função de formatação duplicada` |
| `TasksView.vue`, `DashboardView.vue` | `console.log` esquecido | `console.log esquecido` |
| `TaskDetailView.vue` | Acesso a `task.category.name` sem verificar se `category` é nula | `acesso sem verificação` |
| `authStore.js` e serviços Vue | Falta de testes | `store sem cobertura de testes` |
