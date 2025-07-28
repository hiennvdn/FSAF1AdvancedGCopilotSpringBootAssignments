package com.example.security;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest
public class SecurityConfigTest {

    @Autowired
    private MockMvc mockMvc;

    @Test
    @WithMockUser(roles = "USER")
    public void testDashboardAccessDeniedForUser() throws Exception {
        // This test validates that regular users cannot access the dashboard
        mockMvc.perform(get("/dashboard/stats"))
               .andExpect(status().isForbidden());
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    public void testDashboardAccessAllowedForAdmin() throws Exception {
        // This test validates that admin users can access the dashboard
        mockMvc.perform(get("/dashboard/stats"))
               .andExpect(status().isNotFound()); // 404 because endpoint doesn't exist, but not 403
    }

    @Test
    public void testDashboardAccessDeniedForUnauthenticated() throws Exception {
        // This test validates that unauthenticated users cannot access the dashboard
        mockMvc.perform(get("/dashboard/stats"))
               .andExpect(status().isUnauthorized());
    }

    @Test
    @WithMockUser(roles = "USER")
    public void testOrdersAccessAllowedForUser() throws Exception {
        // This test validates that users can access order endpoints
        mockMvc.perform(get("/api/orders"))
               .andExpect(status().isOk());
    }

    @Test
    public void testPublicEndpointsAccessible() throws Exception {
        // This test validates that public endpoints are accessible
        mockMvc.perform(get("/api/products"))
               .andExpect(status().isOk());
    }
}
