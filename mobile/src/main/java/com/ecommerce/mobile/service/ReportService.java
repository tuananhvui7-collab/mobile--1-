package com.ecommerce.mobile.service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.YearMonth;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.ecommerce.mobile.dto.report.ManagerReportView;
import com.ecommerce.mobile.dto.report.ManagerReportSummary;
import com.ecommerce.mobile.dto.report.MonthlyReportPoint;
import com.ecommerce.mobile.dto.report.StockAlertRow;
import com.ecommerce.mobile.dto.report.TopProductRow;
import com.ecommerce.mobile.entity.Order;
import com.ecommerce.mobile.entity.OrderItem;
import com.ecommerce.mobile.entity.Product;
import com.ecommerce.mobile.entity.ProductVariant;
import com.ecommerce.mobile.enums.OrderStatus;
import com.ecommerce.mobile.enums.ProductStatus;
import com.ecommerce.mobile.repository.OrderRepository;
import com.ecommerce.mobile.repository.ProductRepository;
import com.ecommerce.mobile.repository.ProductVariantRepository;

@Service
public class ReportService {

    private static final int LOW_STOCK_THRESHOLD = 5;
    private static final int MONTH_WINDOW = 6;

    private final OrderRepository orderRepository;
    private final ProductRepository productRepository;
    private final ProductVariantRepository productVariantRepository;

    public ReportService(OrderRepository orderRepository,
                         ProductRepository productRepository,
                         ProductVariantRepository productVariantRepository) {
        this.orderRepository = orderRepository;
        this.productRepository = productRepository;
        this.productVariantRepository = productVariantRepository;
    }

    @Transactional(readOnly = true)
    public ManagerReportView getManagerReport() {
        List<Order> allOrders = orderRepository.findAllByOrderByCreatedAtDesc();
        List<Order> reportableOrders = allOrders.stream()
                .filter(order -> order != null && order.getStatus() != OrderStatus.CANCELLED)
                .toList();
        List<Order> deliveredOrders = allOrders.stream()
                .filter(order -> order != null && order.getStatus() == OrderStatus.DELIVERED)
                .toList();

        List<Product> allProducts = productRepository.findAll();
        List<ProductVariant> allVariants = productVariantRepository.findAll();

        ManagerReportSummary summary = buildSummary(reportableOrders, deliveredOrders, allProducts, allVariants);
        List<MonthlyReportPoint> monthlyPoints = buildMonthlyPoints(reportableOrders, deliveredOrders);
        List<TopProductRow> topProducts = buildTopProducts(deliveredOrders);
        List<StockAlertRow> stockAlerts = buildStockAlerts(allVariants);

        ManagerReportView view = new ManagerReportView();
        view.setSummary(summary);
        view.setMonthlyPoints(monthlyPoints);
        view.setTopProducts(topProducts);
        view.setStockAlerts(stockAlerts);
        return view;
    }

    private ManagerReportSummary buildSummary(List<Order> reportableOrders,
                                              List<Order> deliveredOrders,
                                              List<Product> allProducts,
                                              List<ProductVariant> allVariants) {
        ManagerReportSummary summary = new ManagerReportSummary();
        summary.setTotalOrders(reportableOrders.size());
        summary.setDeliveredOrders(deliveredOrders.size());
        summary.setCancelledOrders((long) orderRepository.findAll().stream()
                .filter(order -> order != null && order.getStatus() == OrderStatus.CANCELLED)
                .count());
        summary.setActiveProducts((long) allProducts.stream()
                .filter(product -> product != null && product.getStatus() == ProductStatus.ACTIVE)
                .count());
        summary.setTotalVariants(allVariants.size());
        summary.setLowStockVariants(allVariants.stream().filter(this::isLowStock).count());
        summary.setTotalStockQty(allVariants.stream()
                .mapToLong(variant -> variant == null || variant.getStockQty() == null ? 0L : variant.getStockQty())
                .sum());

        BigDecimal grossRevenue = sumOrders(reportableOrders);
        BigDecimal realizedRevenue = sumOrders(deliveredOrders);
        BigDecimal realizedCost = sumDeliveryCost(deliveredOrders);
        BigDecimal inventoryValue = sumInventoryValue(allVariants);

        summary.setGrossRevenue(grossRevenue);
        summary.setRealizedRevenue(realizedRevenue);
        summary.setEstimatedCost(realizedCost);
        summary.setEstimatedProfit(realizedRevenue.subtract(realizedCost));
        summary.setInventoryValue(inventoryValue);
        return summary;
    }

    private List<MonthlyReportPoint> buildMonthlyPoints(List<Order> reportableOrders, List<Order> deliveredOrders) {
        Map<YearMonth, MonthlyReportPoint> points = new LinkedHashMap<>();
        YearMonth current = YearMonth.now();
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("MM/yyyy", Locale.getDefault());

        for (int i = MONTH_WINDOW - 1; i >= 0; i--) {
            YearMonth month = current.minusMonths(i);
            MonthlyReportPoint point = new MonthlyReportPoint();
            point.setLabel(month.format(formatter));
            points.put(month, point);
        }

        for (Order order : reportableOrders) {
            if (order == null || order.getCreatedAt() == null) {
                continue;
            }
            YearMonth month = YearMonth.from(order.getCreatedAt());
            MonthlyReportPoint point = points.get(month);
            if (point != null) {
                point.setOrders(point.getOrders() + 1);
                point.setGrossRevenue(point.getGrossRevenue().add(safeBigDecimal(order.getTotalAmount())));
            }
        }

        for (Order order : deliveredOrders) {
            if (order == null || order.getCreatedAt() == null) {
                continue;
            }
            YearMonth month = YearMonth.from(order.getCreatedAt());
            MonthlyReportPoint point = points.get(month);
            if (point != null) {
                point.setRealizedRevenue(point.getRealizedRevenue().add(safeBigDecimal(order.getTotalAmount())));
                point.setEstimatedProfit(point.getEstimatedProfit().add(calculateOrderProfit(order)));
            }
        }

        return new ArrayList<>(points.values());
    }

    private List<TopProductRow> buildTopProducts(List<Order> deliveredOrders) {
        Map<Long, TopProductRow> byProductId = new LinkedHashMap<>();
        for (Order order : deliveredOrders) {
            if (order == null || order.getItems() == null) {
                continue;
            }
            for (OrderItem item : order.getItems()) {
                if (item == null || item.getVariant() == null) {
                    continue;
                }
                Product product = item.getVariant().getProduct();
                Long productId = product == null ? null : product.getProductId();
                if (productId == null) {
                    continue;
                }

                TopProductRow row = byProductId.computeIfAbsent(productId, id -> {
                    TopProductRow created = new TopProductRow();
                    created.setProductId(id);
                    created.setProductName(item.getProductName() != null ? item.getProductName()
                            : (product.getName() != null ? product.getName() : "Sản phẩm"));
                    created.setBrand(product.getBrand());
                    return created;
                });

                long quantity = item.getQuantity() == null ? 0L : item.getQuantity();
                row.setQuantitySold(row.getQuantitySold() + quantity);
                row.setGrossRevenue(row.getGrossRevenue().add(safeBigDecimal(item.getSubtotal())));

                BigDecimal importPrice = safeBigDecimal(item.getVariant().getImportPrice());
                BigDecimal cost = importPrice.multiply(BigDecimal.valueOf(quantity));
                row.setEstimatedCost(row.getEstimatedCost().add(cost));
                row.setEstimatedProfit(row.getGrossRevenue().subtract(row.getEstimatedCost()));
            }
        }

        return byProductId.values().stream()
                .sorted(Comparator.comparing(TopProductRow::getQuantitySold).reversed()
                        .thenComparing(TopProductRow::getGrossRevenue, Comparator.reverseOrder()))
                .limit(10)
                .toList();
    }

    private List<StockAlertRow> buildStockAlerts(List<ProductVariant> allVariants) {
        return allVariants.stream()
                .filter(Objects::nonNull)
                .sorted(Comparator.comparing((ProductVariant v) -> v.getStockQty() == null ? Integer.MAX_VALUE : v.getStockQty())
                        .thenComparing(v -> v.getSku() == null ? "" : v.getSku()))
                .filter(this::isLowStock)
                .map(variant -> {
                    StockAlertRow row = new StockAlertRow();
                    row.setVariantId(variant.getVariant_id());
                    row.setSku(variant.getSku());
                    row.setStorageGb(variant.getStorage_gb());
                    row.setStockQty(variant.getStockQty());
                    row.setPrice(safeBigDecimal(variant.getPrice()));
                    row.setImportPrice(safeBigDecimal(variant.getImportPrice()));
                    row.setInventoryValue(safeBigDecimal(variant.getImportPrice())
                            .multiply(BigDecimal.valueOf(variant.getStockQty() == null ? 0 : variant.getStockQty())));
                    row.setProductName(variant.getProduct() != null ? variant.getProduct().getName() : "—");
                    return row;
                })
                .limit(12)
                .toList();
    }

    private boolean isLowStock(ProductVariant variant) {
        return variant != null
                && variant.getStockQty() != null
                && variant.getStockQty() <= LOW_STOCK_THRESHOLD;
    }

    private BigDecimal sumOrders(List<Order> orders) {
        return orders.stream()
                .map(order -> safeBigDecimal(order.getTotalAmount()))
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    private BigDecimal sumDeliveryCost(List<Order> orders) {
        BigDecimal total = BigDecimal.ZERO;
        for (Order order : orders) {
            if (order == null || order.getItems() == null) {
                continue;
            }
            for (OrderItem item : order.getItems()) {
                if (item == null || item.getVariant() == null) {
                    continue;
                }
                BigDecimal importPrice = safeBigDecimal(item.getVariant().getImportPrice());
                long quantity = item.getQuantity() == null ? 0L : item.getQuantity();
                total = total.add(importPrice.multiply(BigDecimal.valueOf(quantity)));
            }
        }
        return total;
    }

    private BigDecimal sumInventoryValue(List<ProductVariant> variants) {
        BigDecimal total = BigDecimal.ZERO;
        for (ProductVariant variant : variants) {
            if (variant == null) {
                continue;
            }
            BigDecimal importPrice = safeBigDecimal(variant.getImportPrice());
            long stock = variant.getStockQty() == null ? 0L : variant.getStockQty();
            total = total.add(importPrice.multiply(BigDecimal.valueOf(stock)));
        }
        return total;
    }

    private BigDecimal calculateOrderProfit(Order order) {
        if (order == null || order.getItems() == null) {
            return BigDecimal.ZERO;
        }
        BigDecimal revenue = safeBigDecimal(order.getTotalAmount());
        BigDecimal cost = BigDecimal.ZERO;
        for (OrderItem item : order.getItems()) {
            if (item == null || item.getVariant() == null) {
                continue;
            }
            BigDecimal importPrice = safeBigDecimal(item.getVariant().getImportPrice());
            long quantity = item.getQuantity() == null ? 0L : item.getQuantity();
            cost = cost.add(importPrice.multiply(BigDecimal.valueOf(quantity)));
        }
        return revenue.subtract(cost);
    }

    private BigDecimal safeBigDecimal(BigDecimal value) {
        return value == null ? BigDecimal.ZERO : value.setScale(2, RoundingMode.HALF_UP);
    }
}
