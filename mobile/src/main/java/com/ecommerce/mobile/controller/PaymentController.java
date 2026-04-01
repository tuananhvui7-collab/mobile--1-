package com.ecommerce.mobile.controller;

import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import com.ecommerce.mobile.entity.Payment;
import com.ecommerce.mobile.service.PaymentService;

@Controller
public class PaymentController {

    private final PaymentService paymentService;

    public PaymentController(PaymentService paymentService) {
        this.paymentService = paymentService;
    }

    @GetMapping("/payments/{paymentId}")
    public String paymentDetail(@AuthenticationPrincipal UserDetails principal,
                                @PathVariable Long paymentId,
                                Model model,
                                RedirectAttributes redirectAttributes) {
        Payment payment = paymentService.getPaymentForCustomer(paymentId, principal.getUsername());
        if (payment == null) {
            redirectAttributes.addFlashAttribute("error", "Không tìm thấy thanh toán");
            return "redirect:/orders";
        }

        model.addAttribute("payment", payment);
        model.addAttribute("order", payment.getOrder());
        return "payment/detail";
    }

    @PostMapping("/payments/{paymentId}/confirm")
    public String confirmPayment(@AuthenticationPrincipal UserDetails principal,
                                 @PathVariable Long paymentId,
                                 RedirectAttributes redirectAttributes) {
        try {
            Payment payment = paymentService.confirmPayment(paymentId, principal.getUsername());
            redirectAttributes.addFlashAttribute("success", "Đã xác nhận thanh toán thành công");
            return "redirect:/orders/" + payment.getOrder().getOrderId();
        } catch (RuntimeException ex) {
            redirectAttributes.addFlashAttribute("error", ex.getMessage());
            return "redirect:/payments/" + paymentId;
        }
    }
}
