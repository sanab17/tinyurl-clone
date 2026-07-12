package com.urlshortener;

import com.urlshortener.entity.ClickAnalytic;
import com.urlshortener.entity.ShortUrl;
import com.urlshortener.entity.User;
import com.urlshortener.repository.ClickAnalyticRepository;
import com.urlshortener.repository.ShortUrlRepository;
import com.urlshortener.repository.UserRepository;
import org.junit.jupiter.api.Assumptions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.util.TestPropertyValues;
import org.springframework.context.ApplicationContextInitializer;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.transaction.annotation.Transactional;

import java.net.Socket;
import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@ContextConfiguration(initializers = PostgresDbIntegrationTest.PostgresTestInitializer.class)
@Transactional
class PostgresDbIntegrationTest {

    @Value("${postgres.integration.test.enabled:false}")
    private boolean postgresEnabled;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private ShortUrlRepository shortUrlRepository;

    @Autowired
    private ClickAnalyticRepository clickAnalyticRepository;

    @BeforeEach
    void checkConnection() {
        Assumptions.assumeTrue(postgresEnabled, "PostgreSQL is not running on localhost:5432. Skipping PostgreSQL integration tests.");
    }

    @Test
    void testDatabaseOperationsOnPostgres() {
        // Register a user
        User user = new User();
        user.setUsername("pguser");
        user.setPassword("pgpass");
        user = userRepository.save(user);
        assertNotNull(user.getId());

        // Create a short URL
        ShortUrl shortUrl = new ShortUrl();
        shortUrl.setOriginalUrl("https://spring.io");
        shortUrl.setShortCode("spring");
        shortUrl.setTitle("Spring Framework");
        shortUrl.setUser(user);
        shortUrl = shortUrlRepository.save(shortUrl);
        assertNotNull(shortUrl.getId());

        // Create click analytics
        ClickAnalytic click = new ClickAnalytic();
        click.setShortUrl(shortUrl);
        click.setIpAddress("127.0.0.1");
        click.setBrowser("Firefox");
        click.setOperatingSystem("Linux");
        click.setReferrer("direct");
        click.setClickTime(LocalDateTime.now());
        click = clickAnalyticRepository.save(click);
        assertNotNull(click.getId());

        // Verify retrieve works
        User retrievedUser = userRepository.findByUsername("pguser").orElse(null);
        assertNotNull(retrievedUser);
        assertEquals(user.getId(), retrievedUser.getId());

        var urls = shortUrlRepository.findByUserOrderByCreatedAtDesc(retrievedUser);
        assertFalse(urls.isEmpty());
        assertEquals("spring", urls.get(0).getShortCode());

        var clicks = clickAnalyticRepository.findByShortUrl(urls.get(0));
        assertEquals(1, clicks.size());
        assertEquals("Firefox", clicks.get(0).getBrowser());
    }

    public static class PostgresTestInitializer implements ApplicationContextInitializer<ConfigurableApplicationContext> {
        @Override
        public void initialize(ConfigurableApplicationContext context) {
            try (Socket socket = new Socket("localhost", 5432)) {
                // If socket connects successfully, PostgreSQL is running locally
                TestPropertyValues.of(
                        "spring.datasource.url=jdbc:postgresql://localhost:5432/urlshortener",
                        "spring.datasource.driver-class-name=org.postgresql.Driver",
                        "spring.datasource.username=postgres",
                        "spring.datasource.password=postgres",
                        "spring.jpa.database-platform=org.hibernate.dialect.PostgreSQLDialect",
                        "spring.jpa.hibernate.ddl-auto=update",
                        "postgres.integration.test.enabled=true"
                ).applyTo(context.getEnvironment());
            } catch (Exception e) {
                // PostgreSQL not running - fall back to H2 so context loading does not fail
                TestPropertyValues.of(
                        "spring.datasource.url=jdbc:h2:mem:postgresdb;DB_CLOSE_DELAY=-1",
                        "spring.datasource.driver-class-name=org.h2.Driver",
                        "spring.datasource.username=sa",
                        "spring.datasource.password=",
                        "spring.jpa.database-platform=org.hibernate.dialect.H2Dialect",
                        "spring.jpa.hibernate.ddl-auto=update",
                        "postgres.integration.test.enabled=false"
                ).applyTo(context.getEnvironment());
            }
        }
    }
}
