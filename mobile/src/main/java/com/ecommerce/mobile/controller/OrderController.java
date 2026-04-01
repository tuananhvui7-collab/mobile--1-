package com.ecommerce.mobile.controller;

import java.util.List;

import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import com.ecommerce.mobile.entity.Address;
import com.ecommerce.mobile.entity.Cart;
import com.ecommerce.mobile.entity.Order;
import com.ecommerce.mobile.entity.Customer;
import com.ecommerce.mobile.enums.PaymentMethod;
import com.ecommerce.mobile.service.CartService;
import com.ecommerce.mobile.service.CustomerService;
import com.ecommerce.mobile.service.OrderService;

@Controller
public class OrderController {

    private final CartService cartService;
    private final CustomerService customerService;
    private final OrderService orderService;

    public OrderController(CartService cartService,
                           CustomerService customerService,
                           OrderService orderService) {
        this.cartService = cartService;
        this.customerService = customerService;
        this.orderService = orderService;
    }

    @GetMapping("/orders/checkout")
    public String checkout(@AuthenticationPrincipal UserDetails principal,
                           @RequestParam(required = false) Long addressId,
                           Model model,
                           RedirectAttributes redirectAttributes) {
        Customer customer = customerService.requireCustomerByEmail(principal.getUsername());
        Cart cart = cartService.getCartByCustomerEmail(principal.getUsername());

        if (cart.getItems() == null || cart.getItems().isEmpty()) {
            redirectAttributes.addFlashAttribute("error", "Giỏ hàng đang trống");
            return "redirect:/cart";
        }

        List<Address> addresses = customerService.getAddresses(customer.getUserID());
        Address selectedAddress = null;
        try {
            if (addressId != null) {
                selectedAddress = customerService.getAddressForCustomer(customer.getUserID(), addressId);
            } else if (!addresses.isEmpty()) {
                selectedAddress = addresses.stream()
                        .filter(a -> Boolean.TRUE.equals(a.getIsDefault()))
                        .findFirst()
                        .orElse(addresses.get(0));
            }
        } catch (RuntimeException ex) {
            redirectAttributes.addFlashAttribute("error", ex.getMessage());
            return "redirect:/orders/checkout";
        }

        model.addAttribute("addresses", addresses);
        model.addAttribute("selectedAddressId", selectedAddress == null ? null : selectedAddress.getAddressId());
        model.addAttribute("customer", customer);
        model.addAttribute("cart", cart);
        model.addAttribute("total", cartService.calculateTotal(cart));
        model.addAttribute("shippingName", customer.getFullName());
        model.addAttribute("shippingPhone", selectedAddress != null && selectedAddress.getPhone() != null
                ? selectedAddress.getPhone()
                : customer.getPhone());
        model.addAttribute("shippingAddress", selectedAddress != null ? composeAddress(selectedAddress) : "");
        model.addAttribute("shippingCity", selectedAddress != null && selectedAddress.getCity() != null
                ? selectedAddress.getCity()
                : "");
        return "order/checkout";
    }

    @PostMapping("/orders/place")
    public String placeOrder(@AuthenticationPrincipal UserDetails principal,
                             @RequestParam String shippingName,
                             @RequestParam String shippingPhone,
                             @RequestParam String shippingAddress,
                             @RequestParam String shippingCity,
                             @RequestParam(required = false) String voucherCode,
                             @RequestParam String paymentMethod,
                             @RequestParam Long cartId,
                             RedirectAttributes redirectAttributes) {
        try {
            PaymentMethod method = PaymentMethod.valueOf(paymentMethod);
            Order order = orderService.placeOrder(
                    principal.getUsername(),
                    shippingName,
                    shippingPhone,
                    shippingAddress,
                    shippingCity,
                    voucherCode,
                    method,
                    cartId
            );
            redirectAttributes.addFlashAttribute("success", "Đặt hàng thành công: " + order.getOrderCode());
            if (method == PaymentMethod.VN_PAY && order.getLatestPayment() != null) {
                redirectAttributes.addFlashAttribute("success", "Đơn hàng đã tạo. Hãy hoàn tất thanh toán.");
                return "redirect:/payments/" + order.getLatestPayment().getPaymentId();
            }
            return "redirect:/orders/" + order.getOrderId();
        } catch (RuntimeException ex) {
            redirectAttributes.addFlashAttribute("error", ex.getMessage());
            return "redirect:/orders/checkout";
        }
    }

    @GetMapping("/orders")
    public String orderList(@AuthenticationPrincipal UserDetails principal, Model model) {
        List<Order> orders = orderService.getOrdersByCustomerEmail(principal.getUsername());
        model.addAttribute("orders", orders);
        return "order/list";
    }

    @GetMapping("/orders/{orderId}")
    public String orderDetail(@AuthenticationPrincipal UserDetails principal,
                              @PathVariable Long orderId,
                              Model model,
                              RedirectAttributes redirectAttributes) {
        Order order = orderService.getOrderDetailByCustomerEmail(principal.getUsername(), orderId);
        if (order == null) {
            redirectAttributes.addFlashAttribute("error", "Không tìm thấy đơn hàng");
            return "redirect:/orders";
        }

        model.addAttribute("order", order);
        return "order/detail";
    }

    private String composeAddress(Address address) {
        StringBuilder sb = new StringBuilder();
        if (address.getStreet() != null && !address.getStreet().isBlank()) {
            sb.append(address.getStreet().trim());
        }
        if (address.getWard() != null && !address.getWard().isBlank()) {
            appendWithComma(sb, address.getWard().trim());
        }
        if (address.getDistrict() != null && !address.getDistrict().isBlank()) {
            appendWithComma(sb, address.getDistrict().trim());
        }
        return sb.toString();
    }

    private void appendWithComma(StringBuilder sb, String value) {
        if (sb.length() > 0) {
            sb.append(", ");
        }
        sb.append(value);
    }
}
