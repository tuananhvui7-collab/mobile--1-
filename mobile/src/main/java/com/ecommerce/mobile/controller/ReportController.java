package com.ecommerce.mobile.controller;

import com.ecommerce.mobile.dto.report.ManagerReportView;
import com.ecommerce.mobile.service.ReportService;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
@RequestMapping("/admin/reports")
public class ReportController {

    private final ReportService reportService;

    public ReportController(ReportService reportService) {
        this.reportService = reportService;
    }

    @GetMapping
    public String report(Model model) {
        ManagerReportView view = reportService.getManagerReport();
        model.addAttribute("summary", view.getSummary());
        model.addAttribute("monthlyPoints", view.getMonthlyPoints());
        model.addAttribute("topProducts", view.getTopProducts());
        model.addAttribute("stockAlerts", view.getStockAlerts());
        return "admin/reports";
    }
}
