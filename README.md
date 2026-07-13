# URL Shortener

This project explores how far AI can go in building a real-world application—and what still requires engineering judgment. It is a production-inspired URL shortener built with **Java 21**, **Spring Boot 3**, and **Spring Security**, featuring authentication, QR code generation, click analytics, and persistent storage.

🎥 **Built as part of the _"Can AI Build Real Apps?"_ series on Systems with Sana.**

---

## 🚀 Features

- 🔐 User registration and login with BCrypt password hashing
- 🔗 Create short URLs with optional custom aliases
- 📱 QR code generation using ZXing
- 📊 Click analytics (browser, operating system, referrer, timestamps)
- 📈 Interactive dashboard powered by Thymeleaf and Chart.js
- 💾 Persistent PostgreSQL database for storage
- ✅ Alias validation and duplicate detection
- 🛡️ Spring Security route protection

---

## 🛠️ Tech Stack

- Java 21
- Spring Boot 3
- Spring Security 6
- Spring Data JPA
- Hibernate
- Thymeleaf
- Chart.js
- ZXing
- PostgreSQL
- Maven

---

## 🏗️ Project Architecture

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
PostgreSQL Database
```

### Controllers
- **AuthController** – Login and registration
- **DashboardController** – Dashboard, URL creation, analytics
- **RedirectController** – Handles `/{code}` redirects

### Services
- **UserService** – User management
- **ShortUrlService** – URL creation, validation, analytics
- **QrCodeService** – QR code generation
- **UserAgentParser** – Browser and OS detection

### Persistence
- User
- ShortUrl
- ClickAnalytic

---

## ⚡ Getting Started

### Prerequisites

- Java 21
- Maven
- Docker (for local PostgreSQL database)

### Run Locally

1. **Start the local PostgreSQL database** using Docker Compose:
   ```bash
   docker-compose up -d
   ```
   This runs PostgreSQL in the background on port `5432` with username/password/database defaults.

2. **Configure environment variables** (optional, default configurations in `application.properties` match Docker Compose setup):
   - `SPRING_DATASOURCE_URL` – database connection URL (e.g. `jdbc:postgresql://localhost:5432/urlshortener`)
   - `SPRING_DATASOURCE_USERNAME` – database username
   - `SPRING_DATASOURCE_PASSWORD` – database password
   - `SPRING_DATASOURCE_DRIVER_CLASS_NAME` – JDBC driver class name
   - `SPRING_JPA_DATABASE_PLATFORM` – Hibernate dialect

3. **Build and run the application**:
   ```bash
   mvn clean install
   mvn spring-boot:run
   ```

Open:
```
http://localhost:8080
```

---

## ✅ Validation

- Custom alias regex

```
^[a-zA-Z0-9_-]{3,20}$
```

- Duplicate aliases are prevented
- Reserved routes (`login`, `register`, `dashboard`, etc.) cannot be used as aliases
- Redirect route

```
/{code:[a-zA-Z0-9_-]{3,20}}
```

---

## 🧪 Testing

Run the test suite:

```bash
mvn test
```

Tests cover:

- User registration
- Duplicate user detection
- Random short URL generation
- Custom aliases
- Duplicate alias prevention
- Alias validation
- Click tracking
- Analytics persistence

---

## 🔒 Security

- BCrypt password hashing
- Spring Security authentication
- Protected dashboard routes
- Public access for:
  - `/login`
  - `/register`
  - Static assets
  - Redirect endpoints

---

## 🚧 Future Improvements

This project intentionally focuses on a working MVP. Before deploying to production, I would add:

- Redis caching
- CI/CD pipeline
- Monitoring & metrics
- Health checks
- API documentation (OpenAPI/Swagger)

---

## 📊 Logging Strategy

The application implements a production-grade logging strategy using SLF4J and Logback:

- **Business Flow Events (`INFO`):** Logged on successful user registrations, successful login occurrences, short URL creations, custom alias selections, redirections, QR code generations, and deletion actions.
- **Warnings (`WARN`):** Logged on client side validation anomalies (e.g. empty URLs, invalid formatting, duplicate requests), rate limit exclusions, and unauthorized deletion requests.
- **Errors (`ERROR`):** Logged on database access failures or unhandled exceptions, captured centrally by a `GlobalExceptionHandler`.
- **Security Compliance:** Parameters containing passwords, raw credentials, or session values are strictly excluded from output streams. Placeholders (`{}`) are consistently utilized for string construction.

---

## 🎬 AI App Builder Series

This repository is part of my **"Can AI Build Real Apps?"** series, where I evaluate AI-generated code like a real pull request and progressively improve it into a production-ready application.

Follow along as we cover:

- Building the app with AI
- Code review
- Production improvements
- Deployment
- Scaling
- Performance optimization

⭐ If you found this project helpful, consider starring the repository.