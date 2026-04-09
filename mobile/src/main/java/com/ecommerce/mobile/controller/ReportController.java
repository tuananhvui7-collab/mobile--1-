package com.ecommerce.mobile.controller;

import com.ecommerce.mobile.dto.report.ManagerReportView;
import com.ecommerce.mobile.service.ReportService;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
@RequestMapping("/admin/reports")
public class ReportController {

    private final ReportService reportService;
    private final String biDashboardUrl;

    public ReportController(ReportService reportService,
                            @Value("${bi.dashboard-url:}") String biDashboardUrl) {
        this.reportService = reportService;
        this.biDashboardUrl = biDashboardUrl == null ? "" : biDashboardUrl.trim();
    }

    @GetMapping
    public String report(Model model) {
        ManagerReportView view = reportService.getManagerReport();
        boolean biDashboardReady = !biDashboardUrl.isBlank();
        model.addAttribute("summary", view.getSummary());
        model.addAttribute("monthlyPoints", view.getMonthlyPoints());
        model.addAttribute("topProducts", view.getTopProducts());
        model.addAttribute("stockAlerts", view.getStockAlerts());
        model.addAttribute("biDashboardUrl", biDashboardUrl);
        model.addAttribute("biDashboardReady", biDashboardReady);
        return "admin/reports";
    }
}
