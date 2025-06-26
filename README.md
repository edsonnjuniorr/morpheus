# Morpheus

### 🧠 Visão Geral

**Morpheus** é uma assistente virtual pessoal, inspirada no filme *Ela*, voltada para organização e produtividade. Ela permite que o usuário registre tarefas e compromissos com data e hora definidas, recebendo notificações automáticas no dia agendado. A aplicação foi construída com foco em escalabilidade, seguindo os princípios de Clean Code, SOLID e TDD, com arquitetura moderna e preparada para evoluções futuras, como notificações inteligentes, integração com IA e comandos por voz.

---


## ✅ Tecnologias utilizadas

- Java 17
- Spring Boot 3.4.4
- Spring Security 6.4.4
- Spring Data JPA
- MySQL 8.0
- Flyway
- JWT (JJWT 0.11.5)
- Maven

## ⚙️ Funcionalidades

- Registro e login de usuários
- Criação e listagem de eventos pessoais
- Agendador que verifica e notifica eventos pendentes
- Geração e validação de tokens JWT com suporte a roles
- Configuração externa via `application.yml` e variáveis de ambiente
- Repositórios JPA para entidades `User` e `Event`
- Endpoints REST executados em Tomcat embutido
- Mapeamento de exceções e logs via Logback
- Migrações automatizadas com Flyway

## 🔐 Variáveis de ambiente necessárias

```env
MORPHEUS_JWT_SECRET=umaChaveSeguraCom32OuMaisCaracteres
```

## 🚀 Como inicializar

1. Garanta que o MySQL esteja disponível e configure a conexão em
   `src/main/resources/application.yml` caso necessário.
2. Exporte a variável `MORPHEUS_JWT_SECRET` com uma chave segura.
3. Execute o projeto com o Maven:

```bash
mvn spring-boot:run
```

O serviço será iniciado em `http://localhost:8080/api`.

## 📁 Estrutura do projeto

```
src/
├── main/
│   ├── java/com/morpheus/
│   │   ├── MorpheusApplication.java
│   │   ├── config/JwtProperties.java
│   │   ├── security/JwtTokenProvider.java
│   │   └── ... outros pacotes
│   └── resources/
│       ├── application.yml
│       └── db/migration/...
```

## 📚 Como usar

### Registrar usuário

```bash
curl -X POST http://localhost:8080/api/auth/register \
  -H "Content-Type: application/json" \
  -d '{"name":"Seu Nome","email":"voce@email.com","password":"senha"}'
```

### Obter token de acesso

```bash
curl -X POST http://localhost:8080/api/auth/login \
  -H "Content-Type: application/json" \
  -d '{"email":"voce@email.com","password":"senha"}'
```

O retorno conterá o campo `token` que deve ser utilizado no cabeçalho
`Authorization` das requisições autenticadas.

### Criar evento

```bash
curl -X POST http://localhost:8080/api/events \
  -H "Authorization: Bearer <seu-token>" \
  -H "Content-Type: application/json" \
  -d '{"title":"Reunião","description":"Planejar projeto","type":"MEETING","scheduledFor":"2025-01-01T10:00:00"}'
```

### Listar eventos

```bash
curl -H "Authorization: Bearer <seu-token>" http://localhost:8080/api/events
```

## 🚧 Próximas etapas

- Integração com Telegram
- Finalização dos testes unitários e de integração
- Versionamento da API e documentação via Swagger

---

Desenvolvido por Edson Jr.
