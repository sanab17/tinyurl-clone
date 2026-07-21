package com.urlshortener.controller;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
public class ErrorHandlingIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Test
    @WithMockUser
    public void test400Page() throws Exception {
        mockMvc.perform(get("/test-error/400"))
                .andExpect(status().isBadRequest())
                .andExpect(view().name("error/400"))
                .andExpect(content().string(org.hamcrest.Matchers.containsString("400 Bad Request")))
                .andExpect(content().string(org.hamcrest.Matchers.containsString("Bad Request Test")));
    }

    @Test
    @WithMockUser
    public void test401Page() throws Exception {
        mockMvc.perform(get("/test-error/401"))
                .andExpect(status().isUnauthorized())
                .andExpect(view().name("error/401"))
                .andExpect(content().string(org.hamcrest.Matchers.containsString("401 Unauthorized")))
                .andExpect(content().string(org.hamcrest.Matchers.containsString("Unauthorized Test")));
    }

    @Test
    @WithMockUser
    public void test403Page() throws Exception {
        mockMvc.perform(get("/test-error/403"))
                .andExpect(status().isForbidden())
                .andExpect(view().name("error/403"))
                .andExpect(content().string(org.hamcrest.Matchers.containsString("403 Access Denied")))
                .andExpect(content().string(org.hamcrest.Matchers.containsString("Forbidden Test")));
    }

    @Test
    @WithMockUser
    public void test404Page() throws Exception {
        mockMvc.perform(get("/test-error/404"))
                .andExpect(status().isNotFound())
                .andExpect(view().name("error/404"))
                .andExpect(content().string(org.hamcrest.Matchers.containsString("404 Not Found")))
                .andExpect(content().string(org.hamcrest.Matchers.containsString("Not Found Test")));
    }

    @Test
    @WithMockUser
    public void test429Page() throws Exception {
        mockMvc.perform(get("/test-error/429"))
                .andExpect(status().isTooManyRequests())
                .andExpect(view().name("error/429"))
                .andExpect(content().string(org.hamcrest.Matchers.containsString("429 Too Many Requests")))
                .andExpect(content().string(org.hamcrest.Matchers.containsString("Too Many Requests Test")));
    }

    @Test
    @WithMockUser
    public void test500Page() throws Exception {
        mockMvc.perform(get("/test-error/500"))
                .andExpect(status().isInternalServerError())
                .andExpect(view().name("error/500"))
                .andExpect(content().string(org.hamcrest.Matchers.containsString("500 Server Error")))
                .andExpect(content().string(org.hamcrest.Matchers.containsString("An unexpected error occurred. Please try again later.")));
    }
}
