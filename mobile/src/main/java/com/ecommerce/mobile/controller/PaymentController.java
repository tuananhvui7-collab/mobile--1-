package com.ecommerce.mobile.controller;

import jakarta.servlet.http.HttpServletRequest;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import com.ecommerce.mobile.entity.Payment;
import com.ecommerce.mobile.service.PaymentService;
import com.ecommerce.mobile.service.VnpayService;

@Controller
public class PaymentController {

    private final PaymentService paymentService;
    private final VnpayService vnpayService;

    public PaymentController(PaymentService paymentService, VnpayService vnpayService) {
        this.paymentService = paymentService;
        this.vnpayService = vnpayService;
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

    @GetMapping("/payments/{paymentId}/vnpay")
    public String redirectToVnpay(@AuthenticationPrincipal UserDetails principal,
                                  @PathVariable Long paymentId,
                                  HttpServletRequest request,
                                  RedirectAttributes redirectAttributes) {
        Payment payment = paymentService.getPaymentForCustomer(paymentId, principal.getUsername());
        if (payment == null) {
            redirectAttributes.addFlashAttribute("error", "Không tìm thấy thanh toán");
            return "redirect:/orders";
        }
        if (payment.getMethod() != com.ecommerce.mobile.enums.PaymentMethod.VN_PAY) {
            redirectAttributes.addFlashAttribute("error", "Thanh toán này không dùng VNPAY");
            return "redirect:/payments/" + paymentId;
        }
        if (payment.getStatus() == com.ecommerce.mobile.enums.PaymentStatus.SUCCESS) {
            redirectAttributes.addFlashAttribute("success", "Thanh toán đã hoàn tất");
            return "redirect:/orders/" + payment.getOrder().getOrderId();
        }
        String paymentUrl = vnpayService.createPaymentUrl(payment, request);
        return "redirect:" + paymentUrl;
    }

    @GetMapping("/payments/vnpay/return")
    public String vnpayReturn(HttpServletRequest request, Model model) {
        VnpayService.CallbackResult result = vnpayService.processCallback(request, true);
        if (!result.ok()) {
            model.addAttribute("error", result.message());
            model.addAttribute("rspCode", result.rspCode());
            return "payment/return";
        }

        model.addAttribute("payment", result.payment());
        model.addAttribute("order", result.payment().getOrder());
        model.addAttribute("rspCode", result.rspCode());
        model.addAttribute("message", result.message());
        return "payment/return";
    }

    @GetMapping("/payments/vnpay/ipn")
    @ResponseBody
    public java.util.Map<String, String> vnpayIpn(HttpServletRequest request) {
        VnpayService.CallbackResult result = vnpayService.processCallback(request, true);
        return vnpayService.buildIpnResponse(result);
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
