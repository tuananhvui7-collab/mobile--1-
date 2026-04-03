package com.ecommerce.mobile.controller;

import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class HomeController {
    @GetMapping("/")
    public String home() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

        if (authentication != null && authentication.isAuthenticated()) {
            for (GrantedAuthority authority : authentication.getAuthorities()) {
                String role = authority.getAuthority();
                if ("ROLE_MANAGER".equals(role)) {
                    return "redirect:/admin/dashboard";
                }
                if ("ROLE_EMPLOYEE".equals(role)) {
                    return "redirect:/employee/dashboard";
                }
                if ("ROLE_CUSTOMER".equals(role)) {
                    return "redirect:/products";
                }
            }
        }

        return "redirect:/products";
    }
}
