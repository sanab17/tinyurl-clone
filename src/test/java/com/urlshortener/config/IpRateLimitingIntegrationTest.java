package com.urlshortener.config;

import com.urlshortener.service.UserService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.web.servlet.FilterRegistrationBean;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.context.WebApplicationContext;

import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.security.test.web.servlet.setup.SecurityMockMvcConfigurers.springSecurity;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@Transactional
@TestPropertySource(properties = {
        "app.rate-limit.requests-per-minute=2",
        "app.rate-limit.refill-duration=PT1M",
        "app.rate-limit.endpoint=/dashboard/create"
})
class IpRateLimitingIntegrationTest {

    private MockMvc mockMvc;

    @Autowired
    private WebApplicationContext wac;

    @Autowired
    private FilterRegistrationBean<IpRateLimitFilter> ipRateLimitFilterRegistration;

    @Autowired
    private UserService userService;

    @BeforeEach
    void setUp() {
        this.mockMvc = MockMvcBuilders.webAppContextSetup(wac)
                .addFilters(ipRateLimitFilterRegistration.getFilter())
                .apply(springSecurity())
                .build();

        if (userService.findByUsername("testuser").isEmpty()) {
            userService.registerUser("testuser", "password123");
        }
    }

    @Test
    @WithMockUser(username = "testuser")
    void testRateLimitingExceeded() throws Exception {
        // First POST request - Allowed (redirects to /dashboard on success)
        mockMvc.perform(post("/dashboard/create")
                        .servletPath("/dashboard/create")
                        .with(csrf())
                        .param("originalUrl", "https://google.com")
                        .param("title", "Google"))
                .andExpect(status().is3xxRedirection());

        // Second POST request - Allowed
        mockMvc.perform(post("/dashboard/create")
                        .servletPath("/dashboard/create")
                        .with(csrf())
                        .param("originalUrl", "https://github.com")
                        .param("title", "Github"))
                .andExpect(status().is3xxRedirection());

        // Third POST request - Exceeds limit (2 requests per minute), should be rate limited (429)
        mockMvc.perform(post("/dashboard/create")
                        .servletPath("/dashboard/create")
                        .with(csrf())
                        .param("originalUrl", "https://gitlab.com")
                        .param("title", "Gitlab"))
                .andExpect(status().isTooManyRequests())
                .andExpect(header().string("Retry-After", "60"));
    }

    @Test
    @WithMockUser(username = "testuser")
    void testNonRateLimitedEndpointsNotAffected() throws Exception {
        // The rate limit should only apply to POST /dashboard/create.
        // We can make many GET requests to /dashboard without being blocked.
        for (int i = 0; i < 5; i++) {
            mockMvc.perform(get("/dashboard")
                            .servletPath("/dashboard"))
                    .andExpect(status().isOk());
        }
    }
}
