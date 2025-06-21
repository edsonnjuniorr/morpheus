# Morpheus

Morpheus é uma aplicação backend desenvolvida com Spring Boot 3.4.4 e Java 17, com foco em autenticação JWT segura e gerenciamento de eventos de usuários.

## ✅ Tecnologias utilizadas

- Java 17
- Spring Boot 3.4.4
- Spring Security 6.4.4
- Spring Data JPA
- MySQL 8.0
- FlywayDB
- JWT (JJWT 0.11.5)
- Maven

## ⚙️ Funcionalidades implementadas

- [x] Autenticação via JWT
- [x] Criação de tokens com roles
- [x] Validação e parsing de tokens JWT
- [x] Configuração externa via `application.yml` com suporte a variáveis de ambiente
- [x] Entidades `User` e `Event` com repositórios JPA
- [x] Endpoint REST com Tomcat embutido
- [x] Logging com Logback
- [x] Base Flyway estruturada
- [x] DTOs implementados com `record`

## 🔐 Variáveis de ambiente necessárias

```env
MORPHEUS_JWT_SECRET=umaChaveSeguraCom32OuMaisCaracteres
```

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

## 🚧 Próximas etapas

- Integração com Telegram
- Finalização dos testes unitários e de integração
- Versionamento da API e documentação via Swagger

---

Desenvolvido por Edson Jr.
