package com.obp.demo.controller;

import com.obp.demo.dto.CreateUserRequest;
import com.obp.demo.model.ObpUser;
import com.obp.demo.service.AdminAuthService;
import com.obp.demo.service.UserService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.Collections;
import java.util.List;

@Controller
@RequestMapping("/admin")
@RequiredArgsConstructor
@Slf4j
public class AdminController {

    private final UserService userService;
    private final AdminAuthService adminAuthService;

    @GetMapping("/login")
    public String loginPage() {
        return "admin/login";
    }

    @PostMapping("/authenticate")
    public String authenticate(String username, String password, RedirectAttributes redirectAttributes) {
        if (adminAuthService.authenticate(username, password)) {
            Authentication auth = new UsernamePasswordAuthenticationToken(
                    username,
                    null,
                    Collections.singletonList(new SimpleGrantedAuthority("ROLE_ADMIN"))
            );
            SecurityContextHolder.getContext().setAuthentication(auth);
            return "redirect:/admin/dashboard";
        }
        
        redirectAttributes.addFlashAttribute("error", "Invalid credentials");
        return "redirect:/admin/login";
    }

    @GetMapping("/dashboard")
    public String dashboard(Model model) {
        List<ObpUser> users = userService.getAllUsers();
        model.addAttribute("users", users);
        model.addAttribute("createUserRequest", new CreateUserRequest());
        return "admin/dashboard";
    }

    @PostMapping("/users")
    public String createUser(@Valid CreateUserRequest request, 
                            BindingResult bindingResult, 
                            Model model,
                            RedirectAttributes redirectAttributes) {
        if (bindingResult.hasErrors()) {
            model.addAttribute("users", userService.getAllUsers());
            return "admin/dashboard";
        }

        try {
            ObpUser user = userService.createUser(request);
            redirectAttributes.addFlashAttribute("success", 
                "User created successfully: " + user.getUsername());
        } catch (Exception e) {
            log.error("Error creating user", e);
            redirectAttributes.addFlashAttribute("error", 
                "Failed to create user: " + e.getMessage());
        }

        return "redirect:/admin/dashboard";
    }

    @GetMapping("/logout")
    public String logout() {
        SecurityContextHolder.clearContext();
        return "redirect:/admin/login";
    }
}
