package com.ecommerce.mobile.controller;

import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import com.ecommerce.mobile.entity.Customer;
import com.ecommerce.mobile.service.CustomerService;

@Controller
@RequestMapping("/profile")
public class ProfileController {

    private final CustomerService customerService;

    public ProfileController(CustomerService customerService) {
        this.customerService = customerService;
    }

    @GetMapping
    public String editForm(@AuthenticationPrincipal UserDetails principal, Model model) {
        Customer customer = customerService.requireCustomerByEmail(principal.getUsername());
        model.addAttribute("customer", customer);
        return "profile/edit";
    }

    @PostMapping
    public String updateProfile(@AuthenticationPrincipal UserDetails principal,
                                @RequestParam String fullName,
                                @RequestParam(required = false) String phone,
                                RedirectAttributes redirectAttributes) {
        try {
            Customer customer = customerService.requireCustomerByEmail(principal.getUsername());
            customerService.updateCustomerInfo(customer.getUserID(), fullName, phone);
            redirectAttributes.addFlashAttribute("success", "Đã cập nhật thông tin.");
        } catch (RuntimeException ex) {
            redirectAttributes.addFlashAttribute("error", ex.getMessage());
        }
        return "redirect:/profile";
    }

    @PostMapping("/password")
    public String changePassword(@AuthenticationPrincipal UserDetails principal,
                                 @RequestParam String oldPassword,
                                 @RequestParam String newPassword,
                                 @RequestParam String confirmPassword,
                                 RedirectAttributes redirectAttributes) {
        try {
            if (newPassword == null || newPassword.isBlank()) {
                throw new RuntimeException("Mật khẩu mới không được để trống");
            }
            if (!newPassword.equals(confirmPassword)) {
                throw new RuntimeException("Mật khẩu xác nhận không khớp");
            }
            Customer customer = customerService.requireCustomerByEmail(principal.getUsername());
            customerService.changePassword(customer.getUserID(), oldPassword, newPassword);
            redirectAttributes.addFlashAttribute("success", "Đã đổi mật khẩu.");
        } catch (RuntimeException ex) {
            redirectAttributes.addFlashAttribute("error", ex.getMessage());
        }
        return "redirect:/profile";
    }
}
