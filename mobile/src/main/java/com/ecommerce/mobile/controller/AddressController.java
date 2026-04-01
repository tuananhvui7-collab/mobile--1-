package com.ecommerce.mobile.controller;

import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import com.ecommerce.mobile.entity.Address;
import com.ecommerce.mobile.entity.Customer;
import com.ecommerce.mobile.service.CustomerService;

@Controller
@RequestMapping("/profile/addresses")
public class AddressController {

    private final CustomerService customerService;

    public AddressController(CustomerService customerService) {
        this.customerService = customerService;
    }

    @GetMapping
    public String list(@AuthenticationPrincipal UserDetails principal, Model model) {
        Customer customer = customerService.requireCustomerByEmail(principal.getUsername());
        model.addAttribute("customer", customer);
        model.addAttribute("addresses", customerService.getAddresses(customer.getUserID()));
        return "profile/addresses";
    }

    @GetMapping("/add")
    public String addForm(@AuthenticationPrincipal UserDetails principal, Model model) {
        Customer customer = customerService.requireCustomerByEmail(principal.getUsername());
        model.addAttribute("customer", customer);
        model.addAttribute("address", new Address());
        model.addAttribute("action", "/profile/addresses/add");
        model.addAttribute("title", "Thêm địa chỉ");
        model.addAttribute("mode", "create");
        return "profile/address-form";
    }

    @PostMapping("/add")
    public String add(@AuthenticationPrincipal UserDetails principal,
                      @RequestParam String street,
                      @RequestParam(required = false) String ward,
                      @RequestParam(required = false) String district,
                      @RequestParam String city,
                      @RequestParam(required = false) String phone,
                      @RequestParam(defaultValue = "false") boolean setDefault,
                      RedirectAttributes redirectAttributes) {
        try {
            Customer customer = customerService.requireCustomerByEmail(principal.getUsername());
            customerService.addAddress(customer.getUserID(), street, ward, district, city, phone, setDefault);
            redirectAttributes.addFlashAttribute("success", "Đã thêm địa chỉ");
        } catch (RuntimeException ex) {
            redirectAttributes.addFlashAttribute("error", ex.getMessage());
        }
        return "redirect:/profile/addresses";
    }

    @GetMapping("/{addressId}/edit")
    public String editForm(@AuthenticationPrincipal UserDetails principal,
                           @PathVariable Long addressId,
                           Model model) {
        Customer customer = customerService.requireCustomerByEmail(principal.getUsername());
        Address address = customerService.getAddressForCustomer(customer.getUserID(), addressId);
        model.addAttribute("customer", customer);
        model.addAttribute("address", address);
        model.addAttribute("action", "/profile/addresses/" + addressId + "/edit");
        model.addAttribute("title", "Sửa địa chỉ");
        model.addAttribute("mode", "edit");
        return "profile/address-form";
    }

    @PostMapping("/{addressId}/edit")
    public String edit(@AuthenticationPrincipal UserDetails principal,
                       @PathVariable Long addressId,
                       @RequestParam String street,
                       @RequestParam(required = false) String ward,
                       @RequestParam(required = false) String district,
                       @RequestParam String city,
                       @RequestParam(required = false) String phone,
                       RedirectAttributes redirectAttributes) {
        try {
            Customer customer = customerService.requireCustomerByEmail(principal.getUsername());
            customerService.updateAddress(customer.getUserID(), addressId, street, ward, district, city, phone);
            redirectAttributes.addFlashAttribute("success", "Đã cập nhật địa chỉ");
        } catch (RuntimeException ex) {
            redirectAttributes.addFlashAttribute("error", ex.getMessage());
        }
        return "redirect:/profile/addresses";
    }

    @PostMapping("/{addressId}/delete")
    public String delete(@AuthenticationPrincipal UserDetails principal,
                         @PathVariable Long addressId,
                         RedirectAttributes redirectAttributes) {
        try {
            Customer customer = customerService.requireCustomerByEmail(principal.getUsername());
            customerService.deleteAddress(customer.getUserID(), addressId);
            redirectAttributes.addFlashAttribute("success", "Đã xóa địa chỉ");
        } catch (RuntimeException ex) {
            redirectAttributes.addFlashAttribute("error", ex.getMessage());
        }
        return "redirect:/profile/addresses";
    }

    @PostMapping("/{addressId}/default")
    public String setDefault(@AuthenticationPrincipal UserDetails principal,
                             @PathVariable Long addressId,
                             RedirectAttributes redirectAttributes) {
        try {
            Customer customer = customerService.requireCustomerByEmail(principal.getUsername());
            customerService.setDefaultAddress(customer.getUserID(), addressId);
            redirectAttributes.addFlashAttribute("success", "Đã đặt địa chỉ mặc định");
        } catch (RuntimeException ex) {
            redirectAttributes.addFlashAttribute("error", ex.getMessage());
        }
        return "redirect:/profile/addresses";
    }
}
