package com.ecommerce.mobile.controller;

import com.ecommerce.mobile.dto.report.ManagerReportView;
import com.ecommerce.mobile.service.ReportService;

import java.nio.charset.StandardCharsets;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ContentDisposition;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
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
    public String report(@RequestParam(name = "period", defaultValue = "month") String period, Model model) {
        ManagerReportView view = reportService.getManagerReport(period);
        boolean biDashboardReady = !biDashboardUrl.isBlank();
        model.addAttribute("period", view.getPeriodType());
        model.addAttribute("periodLabel", view.getPeriodLabel());
        model.addAttribute("summary", view.getSummary());
        model.addAttribute("periodPoints", view.getPeriodPoints());
        model.addAttribute("topProducts", view.getTopProducts());
        model.addAttribute("stockAlerts", view.getStockAlerts());
        model.addAttribute("biDashboardUrl", biDashboardUrl);
        model.addAttribute("biDashboardReady", biDashboardReady);
        return "admin/reports";
    }

    @GetMapping("/export")
    public ResponseEntity<byte[]> export(@RequestParam(name = "period", defaultValue = "month") String period) {
        byte[] bytes = reportService.exportManagerReportExcel(period);
        String fileName = "phoneshop-report-" + period + ".xlsx";

        return ResponseEntity.ok()
                .contentType(MediaType.parseMediaType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet"))
                .header(HttpHeaders.CONTENT_DISPOSITION,
                        ContentDisposition.attachment().filename(fileName, StandardCharsets.UTF_8).build().toString())
                .body(bytes);
    }
}
