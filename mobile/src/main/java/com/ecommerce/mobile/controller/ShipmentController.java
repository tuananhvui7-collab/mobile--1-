package com.ecommerce.mobile.controller;

import java.util.List;

import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import com.ecommerce.mobile.entity.Order;
import com.ecommerce.mobile.entity.Shipment;
import com.ecommerce.mobile.entity.ShipmentTrackingEvent;
import com.ecommerce.mobile.service.OrderService;
import com.ecommerce.mobile.service.ShipmentService;

@Controller
public class ShipmentController {

    private final OrderService orderService;
    private final ShipmentService shipmentService;

    public ShipmentController(OrderService orderService, ShipmentService shipmentService) {
        this.orderService = orderService;
        this.shipmentService = shipmentService;
    }

    @GetMapping("/orders/{orderId}/tracking")
    public String tracking(@AuthenticationPrincipal UserDetails principal,
                           @PathVariable Long orderId,
                           Model model,
                           RedirectAttributes redirectAttributes) {
        Order order = orderService.getOrderDetailByCustomerEmail(principal.getUsername(), orderId);
        if (order == null) {
            redirectAttributes.addFlashAttribute("error", "Không tìm thấy đơn hàng");
            return "redirect:/orders";
        }

        Shipment shipment = shipmentService.getShipmentForOrder(orderId);
        List<ShipmentTrackingEvent> events = shipmentService.getEventsForOrder(orderId);
        model.addAttribute("order", order);
        model.addAttribute("shipment", shipment);
        model.addAttribute("events", events);
        return "shipment/tracking";
    }

    @PostMapping("/orders/{orderId}/tracking/refresh")
    public String refresh(@AuthenticationPrincipal UserDetails principal,
                          @PathVariable Long orderId,
                          RedirectAttributes redirectAttributes) {
        try {
            Order order = orderService.getOrderDetailByCustomerEmail(principal.getUsername(), orderId);
            if (order == null) {
                redirectAttributes.addFlashAttribute("error", "Không tìm thấy đơn hàng");
                return "redirect:/orders";
            }
            shipmentService.refreshFromGhn(orderId);
            redirectAttributes.addFlashAttribute("success", "Đã đồng bộ trạng thái vận chuyển từ GHN");
        } catch (RuntimeException ex) {
            redirectAttributes.addFlashAttribute("error", ex.getMessage());
        }
        return "redirect:/orders/" + orderId + "/tracking";
    }
}
