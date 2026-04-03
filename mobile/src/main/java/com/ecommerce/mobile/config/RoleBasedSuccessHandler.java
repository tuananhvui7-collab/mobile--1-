package com.ecommerce.mobile.config;

import java.io.IOException;

import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.web.authentication.AuthenticationSuccessHandler;
import org.springframework.stereotype.Component;

@Component
public class RoleBasedSuccessHandler implements AuthenticationSuccessHandler {

    @Override
    public void onAuthenticationSuccess(HttpServletRequest request,
                                        HttpServletResponse response,
                                        Authentication authentication) throws IOException, ServletException {
        String targetUrl = "/";

        for (GrantedAuthority authority : authentication.getAuthorities()) {
            String role = authority.getAuthority();
            if ("ROLE_MANAGER".equals(role)) {
                targetUrl = "/admin/dashboard";
                break;
            }
            if ("ROLE_EMPLOYEE".equals(role)) {
                targetUrl = "/employee/dashboard";
                break;
            }
            if ("ROLE_CUSTOMER".equals(role)) {
                targetUrl = "/products";
            }
        }

        response.sendRedirect(request.getContextPath() + targetUrl);
    }
}
