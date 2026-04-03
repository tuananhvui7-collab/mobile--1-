package com.ecommerce.mobile.controller;

import com.ecommerce.mobile.entity.Product;
import com.ecommerce.mobile.enums.ProductStatus;
import com.ecommerce.mobile.repository.ProductRepository;
import com.ecommerce.mobile.service.ProductService;
import com.ecommerce.mobile.service.ReviewService;

import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;

@Controller
public class ProductController {

    private static final int DEFAULT_PAGE_SIZE = 8;

    @Autowired
    private ProductRepository productRepository;

    @Autowired
    private ProductService productService;

    @Autowired
    private ReviewService reviewService;

    @GetMapping("/products")
public String listProducts(
        @RequestParam(name = "keyword", required = false) String keyword,
        @RequestParam(name = "page", defaultValue = "0") int page,
        Model model) {

    Page<Product> products = productService.getActiveProducts(keyword, page, 8);

    model.addAttribute("products", products);
    model.addAttribute("keyword", keyword == null ? "" : keyword.trim());

    return "product/list";
}


@GetMapping("/products/{id}")
public String productDetail(@PathVariable Long id,
                            @AuthenticationPrincipal UserDetails principal,
                            Model model) {

    Product product = productService.getActiveProductById(id);

    if (product == null) {
        return "redirect:/products?error=not-found";
    }

    model.addAttribute("product", product);
    model.addAttribute("reviews", reviewService.getReviewsForProduct(id));
    model.addAttribute("averageRating", reviewService.getAverageRating(id));
    model.addAttribute("reviewCount", reviewService.getReviewCount(id));
    if (principal != null) {
        model.addAttribute("myReview", reviewService.getMyReview(principal.getUsername(), id));
        model.addAttribute("canReview", reviewService.canReviewProduct(principal.getUsername(), id));
    } else {
        model.addAttribute("myReview", null);
        model.addAttribute("canReview", false);
    }
    return "product/detail";
}
}
