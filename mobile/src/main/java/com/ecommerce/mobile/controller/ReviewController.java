package com.ecommerce.mobile.controller;

import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import com.ecommerce.mobile.service.ReviewService;

@Controller
public class ReviewController {

    private final ReviewService reviewService;

    public ReviewController(ReviewService reviewService) {
        this.reviewService = reviewService;
    }

    @PostMapping("/products/{productId}/reviews")
    public String saveReview(@AuthenticationPrincipal UserDetails principal,
                             @PathVariable Long productId,
                             @RequestParam Integer rating,
                             @RequestParam(required = false) String comment,
                             RedirectAttributes redirectAttributes) {
        try {
            reviewService.saveReview(principal.getUsername(), productId, rating, comment);
            redirectAttributes.addFlashAttribute("success", "Đã lưu đánh giá của bạn");
        } catch (RuntimeException ex) {
            redirectAttributes.addFlashAttribute("error", ex.getMessage());
        }
        return "redirect:/products/" + productId;
    }

    @PostMapping("/products/{productId}/reviews/{reviewId}/delete")
    public String deleteReview(@AuthenticationPrincipal UserDetails principal,
                               @PathVariable Long productId,
                               @PathVariable Long reviewId,
                               RedirectAttributes redirectAttributes) {
        try {
            reviewService.deleteMyReview(principal.getUsername(), reviewId);
            redirectAttributes.addFlashAttribute("success", "Đã xóa đánh giá");
        } catch (RuntimeException ex) {
            redirectAttributes.addFlashAttribute("error", ex.getMessage());
        }
        return "redirect:/products/" + productId;
    }
}
