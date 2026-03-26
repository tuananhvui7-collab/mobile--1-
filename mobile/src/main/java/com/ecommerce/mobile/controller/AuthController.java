
package com.ecommerce.mobile.controller;

import com.ecommerce.mobile.service.CustomerService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.PostMapping;



@Controller
public class AuthController {

    @Autowired
    private CustomerService customerService;

    // Có vẻ bình thường sẽ luôn có cặp getmapping và postmapping. Tuy nhiên nhiều cái cũng tùy,
    // get: tra ve du lieu hien thi\
    // post: gui du lieu di cho he thong xu ly ->redirect cac thu or quay về trang cũ.

    // Hien thi trang dang nhap
    // Spring Security tu xu ly POST /login -- khong can PostMapping
    @GetMapping("/login")
    public String showLogin() {
        return "auth/login";
    }


    // Hien thi trang dang ky
    @GetMapping("/register")
    public String showRegister() {
        return "auth/register";
    }

    // Xu ly form dang ky (POST)
    // @RequestParam: lay gia tri tu input trong form HTML

    @PostMapping("/register")
    public String processRegister(@RequestParam String email, @RequestParam String password, @RequestParam String fullName, @RequestParam String phone, Model model) {
        try{
            customerService.register(email, password, fullName, phone);
            return "redirect:/login?registered=true";
        }catch(Exception e){
            model.addAttribute("error", e.getMessage());
            return "auth/register";
        }
        
    }
    
}
