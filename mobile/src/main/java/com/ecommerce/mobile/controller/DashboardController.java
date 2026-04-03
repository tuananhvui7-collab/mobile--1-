package com.ecommerce.mobile.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class DashboardController {

    @GetMapping("/employee/dashboard")
    public String employeeDashboard() {
        return "dashboard/employee";
    }

    @GetMapping("/admin/dashboard")
    public String adminDashboard() {
        return "dashboard/admin";
    }
}
