package com.ecommerce.mobile.controller;

import java.util.List;

import jakarta.servlet.http.HttpServletRequest;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import com.ecommerce.mobile.entity.Order;
import com.ecommerce.mobile.enums.OrderStatus;
import com.ecommerce.mobile.service.OrderService;

@Controller
public class StaffOrderController {

    private final OrderService orderService;

    public StaffOrderController(OrderService orderService) {
        this.orderService = orderService;
    }

    @GetMapping({"/employee/orders", "/admin/orders"})
    public String list(HttpServletRequest request, Model model) {
        model.addAttribute("orders", orderService.getAllOrdersForStaff());
        model.addAttribute("basePath", basePath(request));
        return "staff/order/list";
    }

    @GetMapping({"/employee/orders/{orderId}", "/admin/orders/{orderId}"})
    public String detail(HttpServletRequest request,
                         @PathVariable Long orderId,
                         Model model,
                         RedirectAttributes redirectAttributes) {
        Order order = orderService.getOrderForStaff(orderId);
        if (order == null) {
            redirectAttributes.addFlashAttribute("error", "Không tìm thấy đơn hàng");
            return "redirect:" + basePath(request);
        }
        model.addAttribute("order", order);
        model.addAttribute("basePath", basePath(request));
        model.addAttribute("nextStatuses", orderService.getAllowedNextStatusesForStaff(orderId));
        model.addAttribute("canCancel", orderService.canStaffCancel(order));
        model.addAttribute("statusGuide", orderService.getStaffStatusGuide(order));
        return "staff/order/detail";
    }

    @PostMapping({"/employee/orders/{orderId}/status", "/admin/orders/{orderId}/status"})
    public String updateStatus(HttpServletRequest request,
                               @PathVariable Long orderId,
                               @RequestParam OrderStatus status,
                               RedirectAttributes redirectAttributes) {
        try {
            orderService.advanceOrderStatusForStaff(orderId, status);
            redirectAttributes.addFlashAttribute("success", "Đã cập nhật trạng thái đơn hàng");
        } catch (RuntimeException ex) {
            redirectAttributes.addFlashAttribute("error", ex.getMessage());
        }
        return "redirect:" + basePath(request) + "/" + orderId;
    }

    @PostMapping({"/employee/orders/{orderId}/receive", "/admin/orders/{orderId}/receive"})
    public String receiveOrder(HttpServletRequest request,
                               @PathVariable Long orderId,
                               RedirectAttributes redirectAttributes) {
        try {
            orderService.receiveOrderForStaff(orderId);
            redirectAttributes.addFlashAttribute("success", "Đã tiếp nhận đơn hàng");
        } catch (RuntimeException ex) {
            redirectAttributes.addFlashAttribute("error", ex.getMessage());
        }
        return "redirect:" + basePath(request) + "/" + orderId;
    }

    private String basePath(HttpServletRequest request) {
        String uri = request.getRequestURI();
        return uri.startsWith("/admin") ? "/admin/orders" : "/employee/orders";
    }
}
