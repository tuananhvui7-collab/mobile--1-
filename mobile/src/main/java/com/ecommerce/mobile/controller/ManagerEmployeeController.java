package com.ecommerce.mobile.controller;

import java.math.BigDecimal;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import com.ecommerce.mobile.service.ManagerEmployeeService;

@Controller
@RequestMapping("/admin/employees")
public class ManagerEmployeeController {

    private final ManagerEmployeeService managerEmployeeService;

    public ManagerEmployeeController(ManagerEmployeeService managerEmployeeService) {
        this.managerEmployeeService = managerEmployeeService;
    }

    @GetMapping
    public String list(Model model) {
        model.addAttribute("employees", managerEmployeeService.getAllEmployees());
        return "manager/employee/list";
    }

    @GetMapping("/create")
    public String createForm(Model model) {
        model.addAttribute("employee", null);
        return "manager/employee/form";
    }

    @GetMapping("/{id}")
    public String detail(@PathVariable Long id, Model model) {
        model.addAttribute("employee", managerEmployeeService.getEmployee(id));
        return "manager/employee/detail";
    }

    @GetMapping("/{id}/edit")
    public String editForm(@PathVariable Long id, Model model) {
        model.addAttribute("employee", managerEmployeeService.getEmployee(id));
        return "manager/employee/form";
    }

    @PostMapping("/save")
    public String save(@RequestParam(required = false) Long employeeId,
                       @RequestParam String email,
                       @RequestParam(required = false) String password,
                       @RequestParam String fullName,
                       @RequestParam(required = false) String phone,
                       @RequestParam(required = false) BigDecimal salary,
                       @RequestParam(required = false, defaultValue = "true") Boolean active,
                       RedirectAttributes redirectAttributes) {
        try {
            if (employeeId == null) {
                managerEmployeeService.createEmployee(email, password, fullName, phone, salary, active);
                redirectAttributes.addFlashAttribute("success", "Đã tạo nhân viên mới");
            } else {
                managerEmployeeService.updateEmployee(employeeId, fullName, phone, salary, active, password);
                redirectAttributes.addFlashAttribute("success", "Đã cập nhật nhân viên");
            }
            return "redirect:/admin/employees";
        } catch (RuntimeException ex) {
            redirectAttributes.addFlashAttribute("error", ex.getMessage());
            return employeeId == null ? "redirect:/admin/employees/create" : "redirect:/admin/employees/" + employeeId + "/edit";
        }
    }

    @PostMapping("/{id}/activate")
    public String activate(@PathVariable Long id, RedirectAttributes redirectAttributes) {
        try {
            managerEmployeeService.setActive(id, true);
            redirectAttributes.addFlashAttribute("success", "Đã kích hoạt nhân viên");
        } catch (RuntimeException ex) {
            redirectAttributes.addFlashAttribute("error", ex.getMessage());
        }
        return "redirect:/admin/employees";
    }

    @PostMapping("/{id}/deactivate")
    public String deactivate(@PathVariable Long id, RedirectAttributes redirectAttributes) {
        try {
            managerEmployeeService.setActive(id, false);
            redirectAttributes.addFlashAttribute("success", "Đã khóa nhân viên");
        } catch (RuntimeException ex) {
            redirectAttributes.addFlashAttribute("error", ex.getMessage());
        }
        return "redirect:/admin/employees";
    }

    @PostMapping("/{id}/reset-password")
    public String resetPassword(@PathVariable Long id,
                                @RequestParam String newPassword,
                                RedirectAttributes redirectAttributes) {
        try {
            managerEmployeeService.resetPassword(id, newPassword);
            redirectAttributes.addFlashAttribute("success", "Đã đặt lại mật khẩu cho nhân viên");
        } catch (RuntimeException ex) {
            redirectAttributes.addFlashAttribute("error", ex.getMessage());
        }
        return "redirect:/admin/employees/" + id;
    }
}
