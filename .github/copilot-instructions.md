# AI Coding Agent Instructions for AetherLink URL Shortener

## Project Overview
AetherLink is a Spring Boot 3.3.1 URL shortening service with authentication, link analytics, and QR code generation. The architecture uses a three-layer pattern (Controller → Service → Repository) with H2 file-based persistence.

## Architecture & Data Flow

### Core Components
- **Auth Layer**: `AuthController` → `UserService` (BCrypt password encoding via `SecurityConfig`)
- **Link Management**: `DashboardController` → `ShortUrlService` → `ShortUrlRepository`
- **Analytics**: `DashboardController` collects click metrics, `RedirectController` captures visitor details
- **QR Codes**: Generated server-side by `QrCodeService` (ZXing library), stored as Base64 in `ShortUrl.qrCodeBase64`

### Request Flow for Short Link Creation
1. User submits form in `dashboard.html` → `POST /dashboard/create`
2. `DashboardController.createShortUrl()` delegates to `ShortUrlService.createShortUrl()`
3. Service validates custom alias (3-20 chars, alphanumeric + `-_`), generates random 6-char code if needed
4. Reserved keywords (`login`, `register`, `dashboard`, etc.) are blocked
5. QR code generated and stored; `ShortUrl` entity saved to H2 database

### Click Tracking Flow
1. Redirect hit `GET /{code}` → `RedirectController.redirect()`
2. Captures IP (with `X-Forwarded-For` header support), User-Agent, Referer
3. `UserAgentParser` extracts browser and OS (e.g., "Chrome", "macOS")
4. `ClickAnalytic` record persisted; `ShortUrl.clickCount` incremented
5. Analytics aggregated in `DashboardController.viewAnalytics()` using Java Streams (grouped by date, browser, OS, referrer)

### Frontend Rendering
- **Thymeleaf templates** receive aggregated analytics as Maps (e.g., `clicksByDate: {"2026-06-28": 5}`)
- **Chart.js** renders line chart (clicks over time), doughnut charts (browsers/OS), bar chart (referrers)
- **Cyber-glass design**: CSS variables (`--accent-blue`, `--glass-bg`) define dark theme with gradient accents

## Key Patterns & Conventions

### Service Layer Validation
Services throw `IllegalArgumentException` with descriptive messages (caught by controllers and passed to UI):
```java
// Pattern: Validate before persisting
if (originalUrl == null || originalUrl.isBlank()) {
    throw new IllegalArgumentException("Original URL cannot be empty");
}
```

### Custom Alias Validation
Regex pattern: `^[a-zA-Z0-9_-]{3,20}$` (enforced in both service and controller route regex `/{code:[a-zA-Z0-9_-]{3,20}}`)

### User Ownership Verification
Always check `shortUrl.getUser().getId().equals(user.getId())` before allowing modifications (see `viewAnalytics()` and `deleteShortUrl()`)

### Database Timestamps
Use `LocalDateTime.now()` in `@PrePersist` methods for consistency; stored/retrieved as-is (H2 default behavior)

### User-Agent Parsing
`UserAgentParser` contains static methods mapping UA strings to browser/OS names. Handles Chrome, Firefox, Safari, Edge. Returns "Unknown" for unrecognized agents.

## Development Workflow

### Build & Run
```bash
mvn clean install          # Compile, run tests
mvn spring-boot:run        # Start server on :8080
```

### Testing
- Integration tests in `UrlShortenerApplicationTests.java` use `@SpringBootTest` + `@Transactional`
- Test fixtures created in `@BeforeEach` (user registration, URL creation)
- Assert custom alias validation, duplicate detection, click recording with correct parsing

### Database
- H2 file-based: `./data/urlshortener` (created on first run)
- Schema auto-updated via `spring.jpa.hibernate.ddl-auto=update`
- Console accessible at `/h2-console` (permitted in `SecurityConfig`)

### Security Routes
- Public: `/login`, `/register`, `/css/**`, `/js/**`, `/{code}` (redirect)
- Protected: `/dashboard`, `/dashboard/**` (require authentication)
- H2 console: Public but frame-same-origin + CSRF ignored

## Code Examples for Common Tasks

### Adding a New Short Link Validation
```java
// In ShortUrlService.createShortUrl()
if (originalUrl.length() > 2048) {
    throw new IllegalArgumentException("URL exceeds maximum length");
}
```

### Extending Analytics Metrics
1. Add new `Map<String, Long>` computation in `DashboardController.viewAnalytics()`:
   ```java
   Map<String, Long> clicksByCountry = clickLogs.stream()
       .collect(Collectors.groupingBy(ClickAnalytic::getCountry, Collectors.counting()));
   ```
2. Pass to model: `model.addAttribute("clicksByCountry", clicksByCountry)`
3. Inject in `analytics.html`: `window.analyticsData.clicksByCountry`
4. Render Chart.js visualization in `analytics.js`

### Adding a New Entity
1. Create `@Entity` class in `entity/` with `@PrePersist` for timestamps
2. Create corresponding `@Repository` interface extending `JpaRepository<YourEntity, Long>`
3. Wire into service via constructor injection
4. Add integration tests before deploying

## External Dependencies
- **Spring Boot 3.3.1**: Web framework, security, JPA/Hibernate
- **H2 Database**: File-based SQL database (zero configuration)
- **ZXing 3.5.3**: QR code generation (uses `MatrixToImageWriter` for PNG output)
- **Thymeleaf**: Server-side template rendering
- **Chart.js**: Client-side analytics visualization (CDN-loaded)

## IDE Setup & Debugging
- Java 21 required (specified in `pom.xml`)
- Maven projects auto-recognized in VS Code with Spring extension
- Debug breakpoints in services; use `localhost:8080/h2-console` to inspect schema/data
- Hot reload: Modify templates and refresh; services require rebuild

## Cautionary Notes
- **No API versioning**: Redirect responses are simple 302 redirects; changes to controller paths break existing links
- **QR generation can fail silently**: `QrCodeService` returns null on error; ensure DB migration if adding NOT NULL constraint
- **Click timestamps are server-local**: No timezone consideration; assumes single server deployment
- **No rate limiting**: Anonymous redirect traffic can spike analytics tables; add pagination if needed for large datasets
