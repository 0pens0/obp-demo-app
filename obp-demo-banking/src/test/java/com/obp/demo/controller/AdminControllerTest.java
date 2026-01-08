package com.obp.demo.controller;

import com.obp.demo.model.ObpUser;
import com.obp.demo.service.AdminAuthService;
import com.obp.demo.service.UserService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;

import java.util.Arrays;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * Integration tests for AdminController using MockMvc.
 */
@WebMvcTest(AdminController.class)
class AdminControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private UserService userService;

    @MockBean
    private AdminAuthService adminAuthService;

    @Test
    void testLoginPage() throws Exception {
        mockMvc.perform(get("/admin/login"))
                .andExpect(status().isOk())
                .andExpect(view().name("admin/login"));
    }

    @Test
    void testAuthenticate_Success() throws Exception {
        // Given
        when(adminAuthService.authenticate("admin", "admin")).thenReturn(true);

        // When & Then
        mockMvc.perform(post("/admin/authenticate")
                        .param("username", "admin")
                        .param("password", "admin")
                        .with(csrf()))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/admin/dashboard"));
    }

    @Test
    void testAuthenticate_Failure() throws Exception {
        // Given
        when(adminAuthService.authenticate("admin", "wrong")).thenReturn(false);

        // When & Then
        mockMvc.perform(post("/admin/authenticate")
                        .param("username", "admin")
                        .param("password", "wrong")
                        .with(csrf()))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/admin/login"));
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void testDashboard() throws Exception {
        // Given
        List<ObpUser> users = Arrays.asList(new ObpUser());
        when(userService.getAllUsers()).thenReturn(users);

        // When & Then
        mockMvc.perform(get("/admin/dashboard"))
                .andExpect(status().isOk())
                .andExpect(view().name("admin/dashboard"))
                .andExpect(model().attributeExists("users"))
                .andExpect(model().attributeExists("createUserRequest"));
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void testLogout() throws Exception {
        mockMvc.perform(get("/admin/logout"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/admin/login"));
    }
}
