# tinyurl-clone

A Spring Boot URL shortener with authentication, analytics, QR code support, and H2 persistence.

## Features

- User registration and login with BCrypt password hashing
- Create short URLs with optional custom alias
- QR code generation stored as Base64 in `ShortUrl.qrCodeBase64`
- Redirect tracking with browser / OS / referrer analytics
- Dashboard rendered with Thymeleaf + Chart.js
- H2 file-based database for local development

## Architecture

- `src/main/java/com/urlshortener/controller`
  - `AuthController` handles `/login`, `/register`
  - `DashboardController` handles `/dashboard/**`
  - `RedirectController` handles `GET /{code}` redirects
- `src/main/java/com/urlshortener/service`
  - `UserService` manages users
  - `ShortUrlService` manages link creation, alias validation, click recording
  - `QrCodeService` generates QR PNG data
- `src/main/java/com/urlshortener/repository`
  - JPA repositories for `User`, `ShortUrl`, `ClickAnalytic`
- `src/main/java/com/urlshortener/entity`
  - Domain models with JPA mappings and audit timestamps

## Getting Started

Requirements:
- Java 21
- Maven

Commands:
```bash
mvn clean install
mvn spring-boot:run
mvn test

## ==================================

Open the app at:
- http://localhost:8080

H2 console: 
- http://localhost:8080/h2-console

H2 file database is stored in:
- ./data/urlshortener

Validation and Rules

- Custom alias regex: ^[a-zA-Z0-9_-]{3,20}$
- Reserved paths are blocked for aliases, including login, register, dashboard
- ShortUrlService.createShortUrl() validates URL, alias uniqueness, and alias format
- Redirect route pattern: /{code:[a-zA-Z0-9_-]{3,20}}

Testing

- Integration tests live in src/test/java/com/urlshortener/UrlShortenerApplicationTests.java
- Uses @SpringBootTest + @Transactional

Covers:
* user registration and duplicate username handling
* random and custom alias creation
* invalid alias rejection
* click tracking and analytics persistence

Notes

- Security config allows public access to /login, /register, static assets, and redirect paths
- Protected routes under /dashboard/** require authentication
- Schema is managed by Hibernate with spring.jpa.hibernate.ddl-auto=update
- Click analytics are computed from ClickAnalytic records and shown in dashboard charts
