package com.ecommerce.mobile.dto.report;

import java.util.List;

import lombok.Data;

@Data
public class ManagerReportView {
    private ManagerReportSummary summary;
    private List<MonthlyReportPoint> monthlyPoints;
    private List<TopProductRow> topProducts;
    private List<StockAlertRow> stockAlerts;
}
