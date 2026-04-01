package com.ecommerce.mobile.controller;

import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import com.ecommerce.mobile.entity.Cart;
import com.ecommerce.mobile.service.CartService;

@Controller
public class CartController {

    private final CartService cartService;

    public CartController(CartService cartService) {
        this.cartService = cartService;
    }

    @GetMapping("/cart")
    public String viewCart(@AuthenticationPrincipal UserDetails principal, Model model) {
        Cart cart = cartService.getCartByCustomerEmail(principal.getUsername());
        model.addAttribute("cart", cart);
        model.addAttribute("total", cartService.calculateTotal(cart));
        return "cart/view";
    }

    @PostMapping("/cart/add/{variantId}")
    public String addToCart(@AuthenticationPrincipal UserDetails principal,
                            @PathVariable Long variantId,
                            @RequestParam(defaultValue = "1") Integer quantity,
                            RedirectAttributes redirectAttributes) {
        try {
            cartService.addToCart(principal.getUsername(), variantId, quantity);
            redirectAttributes.addFlashAttribute("success", "Đã thêm vào giỏ hàng");
        } catch (RuntimeException ex) {
            redirectAttributes.addFlashAttribute("error", ex.getMessage());
        }
        return "redirect:/cart";
    }

    @PostMapping("/cart/update/{itemId}")
    public String updateItem(@AuthenticationPrincipal UserDetails principal,
                             @PathVariable Long itemId,
                             @RequestParam Integer quantity,
                             RedirectAttributes redirectAttributes) {
        try {
            cartService.updateItemQuantity(principal.getUsername(), itemId, quantity);
            redirectAttributes.addFlashAttribute("success", "Đã cập nhật giỏ hàng");
        } catch (RuntimeException ex) {
            redirectAttributes.addFlashAttribute("error", ex.getMessage());
        }
        return "redirect:/cart";
    }

    @PostMapping("/cart/remove/{itemId}")
    public String removeItem(@AuthenticationPrincipal UserDetails principal,
                             @PathVariable Long itemId,
                             RedirectAttributes redirectAttributes) {
        try {
            cartService.removeItem(principal.getUsername(), itemId);
            redirectAttributes.addFlashAttribute("success", "Đã xóa sản phẩm khỏi giỏ");
        } catch (RuntimeException ex) {
            redirectAttributes.addFlashAttribute("error", ex.getMessage());
        }
        return "redirect:/cart";
    }
}
