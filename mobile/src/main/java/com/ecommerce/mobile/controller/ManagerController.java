package com.ecommerce.mobile.controller;

import com.ecommerce.mobile.dto.manager.ManagerProductForm;
import com.ecommerce.mobile.entity.Product;
import com.ecommerce.mobile.service.ManagerService;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
@RequestMapping("/admin/products")
public class ManagerController {

    private final ManagerService managerService;

    public ManagerController(ManagerService managerService) {
        this.managerService = managerService;
    }

    @GetMapping
    public String list(Model model) {
        model.addAttribute("products", managerService.getAllProducts());
        return "manager/product/list";
    }

    @GetMapping("/create")
    public String createForm(Model model) {
        model.addAttribute("form", managerService.newProductForm());
        model.addAttribute("categories", managerService.getAllCategories());
        return "manager/product/form";
    }

    @PostMapping("/save")
    public String save(@ModelAttribute("form") ManagerProductForm form,
                       Model model,
                       RedirectAttributes redirectAttributes) {
        try {
            Product saved = managerService.saveProduct(form);
            redirectAttributes.addFlashAttribute("success", "Đã lưu sản phẩm");
            return "redirect:/admin/products/" + saved.getProductId();
        } catch (Exception ex) {
            model.addAttribute("error", ex.getMessage());
            model.addAttribute("categories", managerService.getAllCategories());
            model.addAttribute("form", form);
            return "manager/product/form";
        }
    }

    @GetMapping("/{id}")
    public String detail(@PathVariable Long id, Model model) {
        model.addAttribute("product", managerService.getProductDetail(id));
        return "manager/product/detail";
    }

    @GetMapping("/{id}/edit")
    public String editForm(@PathVariable Long id, Model model) {
        model.addAttribute("form", managerService.loadProductForm(id));
        model.addAttribute("categories", managerService.getAllCategories());
        return "manager/product/form";
    }

    @PostMapping("/{id}/delete")
    public String delete(@PathVariable Long id, RedirectAttributes redirectAttributes) {
        managerService.softDeleteProduct(id);
        redirectAttributes.addFlashAttribute("success", "Đã chuyển sản phẩm sang INACTIVE");
        return "redirect:/admin/products";
    }

    @PostMapping("/{id}/restore")
    public String restore(@PathVariable Long id, RedirectAttributes redirectAttributes) {
        managerService.restoreProduct(id);
        redirectAttributes.addFlashAttribute("success", "Đã kích hoạt lại sản phẩm");
        return "redirect:/admin/products";
    }
}
