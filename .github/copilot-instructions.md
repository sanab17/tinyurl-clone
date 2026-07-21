# Purpose

This project is part of the **"Can AI Build Real Apps?"** series on **Systems with Sana**. The goal is to explore AI-assisted software development by building production-inspired applications while reviewing and improving the generated code like a real engineering code review.

When generating code:

- Prefer clean, maintainable, production-inspired solutions.
- Explain important architectural decisions when they are not obvious.
- Avoid unnecessary complexity or over-engineering.
- Generate code that is educational and follows Spring Boot best practices.
- Prioritize readability, maintainability, and consistency over clever implementations.

---

# Coding Standards

- Use constructor injection only. Never use field injection.
- Keep controllers thin; business logic belongs in the service layer.
- Services should not depend on controllers.
- Repository interfaces should contain only persistence logic.
- Validate all user input in the service layer before persisting data.
- Throw descriptive `IllegalArgumentException` messages for validation failures.
- Follow existing naming conventions throughout the project.
- Keep methods focused on a single responsibility.
- Prefer immutable objects where practical.
- Reuse existing services and utilities instead of duplicating logic.

---

# Spring Boot Guidelines

- Use `@Service` for business logic.
- Use `@Transactional` where data consistency is required.
- Prefer constructor injection.
- Use `Optional` instead of returning `null` where appropriate.
- Keep Thymeleaf controllers returning view names.
- Follow the existing layered architecture:
  ```
  Controller
      ↓
    Service
      ↓
   Repository
      ↓
    Database
  ```
- Keep controller methods concise and delegate logic to services.

---

# Security Requirements

Whenever adding or modifying features:

- Never expose dashboard functionality without authentication.
- Validate all user input.
- Continue using `BCryptPasswordEncoder` for password hashing.
- Prevent duplicate aliases using both service validation and database constraints.
- Protect against common vulnerabilities such as invalid input and malicious requests.
- Preserve existing Spring Security configuration unless explicitly changing authentication behavior.

---

# Production Mindset

Always prefer production-ready implementations over demo shortcuts.

When implementing new features, consider:

- Scalability
- Concurrency
- Security
- Input validation
- Logging
- Monitoring
- Testability
- Error handling

If multiple implementation approaches exist, prefer the one suitable for long-term production maintenance.

---

# Testing Expectations

Every new feature should include appropriate tests.

Prefer integration tests using:

- `@SpringBootTest`
- `@Transactional`

Tests should verify:

- Happy path
- Validation failures
- Authorization
- Duplicate data handling
- Edge cases

Avoid adding features without corresponding test coverage.

---

# AI Assistant Behavior

When modifying existing code:

- Reuse existing services whenever possible.
- Preserve the current architecture.
- Do not introduce unnecessary frameworks or libraries.
- Do not rewrite working code unless explicitly requested.
- Keep code style consistent with the existing project.
- Explain significant architectural changes before implementing them.
- Generate code that is easy for other developers to understand and maintain.
- If a production concern is identified (security, scalability, concurrency, etc.), mention it and suggest improvements.

---

# Project Architecture

The project follows a standard Spring Boot layered architecture.

```
Client
   │
   ▼
Controllers
   │
   ▼
Services
   │
   ▼
Repositories
   │
   ▼
Database
```

### Controllers

- `AuthController` — Login and registration
- `DashboardController` — Dashboard, URL creation, analytics
- `RedirectController` — URL redirection and click tracking

### Services

- `UserService` — User management
- `ShortUrlService` — URL creation, validation, analytics
- `QrCodeService` — QR code generation
- `UserAgentParser` — Browser and OS detection

### Persistence

- `User`
- `ShortUrl`
- `ClickAnalytic`

---

# Validation Rules

- Custom alias regex:

```
^[a-zA-Z0-9_-]{3,20}$
```

- Reserved aliases (`login`, `register`, `dashboard`, etc.) are blocked.
- Duplicate aliases are validated before persistence.
- Redirect route:

```
/{code:[a-zA-Z0-9_-]{3,20}}
```

---

# Development Workflow

Build:

```bash
mvn clean install
```

Run:

```bash
mvn spring-boot:run
```

Test:

```bash
mvn test
```

Database:

- H2 file-based database (`./data/urlshortener`)
- Hibernate schema auto-update enabled
- H2 Console available at `/h2-console`

---

# Future Enhancements

When extending this project, prefer implementations that support:

- PostgreSQL
- Redis caching
- Rate limiting
- Docker
- CI/CD pipelines
- Health checks
- Structured logging
- Monitoring and metrics
- OpenAPI / Swagger documentation
- Cloud deployment readiness