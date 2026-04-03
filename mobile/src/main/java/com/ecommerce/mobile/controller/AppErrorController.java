package com.ecommerce.mobile.controller;

import jakarta.servlet.http.HttpServletRequest;

import org.springframework.boot.web.servlet.error.ErrorController;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
public class AppErrorController implements ErrorController {

    @RequestMapping("/error")
    public String handleError(HttpServletRequest request, Model model) {
        Object statusCode = request.getAttribute("jakarta.servlet.error.status_code");
        Object requestUri = request.getAttribute("jakarta.servlet.error.request_uri");
        Object exception = request.getAttribute("jakarta.servlet.error.exception");
        Object message = request.getAttribute("jakarta.servlet.error.message");

        model.addAttribute("status", statusCode != null ? statusCode.toString() : "500");
        model.addAttribute("path", requestUri != null ? requestUri.toString() : request.getRequestURI());
        model.addAttribute("message", message != null ? message.toString() : "Đã có lỗi xảy ra trong quá trình xử lý.");
        model.addAttribute("exception", exception != null ? exception.toString() : null);

        return "error";
    }
}
