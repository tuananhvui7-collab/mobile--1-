package com.ecommerce.mobile.controller;

import com.ecommerce.mobile.entity.Product;
import com.ecommerce.mobile.enums.ProductStatus;
import com.ecommerce.mobile.repository.ProductRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestParam;

@Controller
public class ProductController {

    private static final int DEFAULT_PAGE_SIZE = 8;

    @Autowired
    private ProductRepository productRepository;

    @GetMapping("/products")
    public String listProducts(
            @RequestParam(name = "keyword", required = false) String keyword,
            @RequestParam(name = "page", defaultValue = "0") int page,
            Model model) {

        PageRequest pageable = PageRequest.of(
                Math.max(page, 0),
                DEFAULT_PAGE_SIZE,
                Sort.by(Sort.Direction.DESC, "createdAt"));

        String normalizedKeyword = keyword == null ? "" : keyword.trim();
        Page<Product> products = normalizedKeyword.isEmpty()
                ? productRepository.findByStatus(ProductStatus.ACTIVE, pageable)
                : productRepository.searchByStatusAndKeyword(ProductStatus.ACTIVE, normalizedKeyword, pageable);

        model.addAttribute("products", products);
        model.addAttribute("keyword", normalizedKeyword);
        return "product/list";
    }

    @GetMapping("/products/{id}")
    public String productDetail(@PathVariable Long id, Model model) {
        Product product = productRepository.findById(id)
                .filter(p -> p.getStatus() == ProductStatus.ACTIVE)
                .orElse(null);

        if (product == null) {
            return "redirect:/products?error=not-found";
        }

        model.addAttribute("product", product);
        return "product/detail";

        // cái này nghiên cưu sau
    }
}
