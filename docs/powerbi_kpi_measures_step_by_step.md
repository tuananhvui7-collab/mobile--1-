# Power BI Measures - Step by Step

Power BI does not run this whole file at once.
Create each item below as a separate **New measure** in the exact order shown.

## Group 1 - Core sales

### 1. Total Revenue
```DAX
Total Revenue = SUM(Fact_Sales[revenue])
```

### 2. Total COGS
```DAX
Total COGS = SUM(Fact_Sales[cogs])
```

### 3. Total Discounts
```DAX
Total Discounts =
SUMX(
    VALUES(Fact_Sales[order_id]),
    CALCULATE(MAX(Fact_Sales[discount_amount]))
)
```

### 4. Total Shipping Fee
```DAX
Total Shipping Fee =
SUMX(
    VALUES(Fact_Sales[order_id]),
    CALCULATE(MAX(Fact_Sales[shipping_fee]))
)
```

### 5. Total Orders
```DAX
Total Orders = DISTINCTCOUNT(Fact_Sales[order_id])
```

### 6. Total Units Sold
```DAX
Total Units Sold = SUM(Fact_Sales[quantity])
```

## Group 2 - Derived sales metrics

### 7. Net Revenue
```DAX
Net Revenue = [Total Revenue] - [Total Discounts]
```

### 8. Gross Profit
```DAX
Gross Profit = [Net Revenue] - [Total COGS]
```

### 9. Gross Margin %
```DAX
Gross Margin % =
DIVIDE([Gross Profit], [Net Revenue], 0)
```

### 10. AOV
```DAX
AOV = DIVIDE([Net Revenue], [Total Orders], 0)
```

### 11. Average Lead Time (Days)
```DAX
Average Lead Time (Days) =
AVERAGEX(
    VALUES(Fact_Sales[order_id]),
    CALCULATE(MAX(Fact_Sales[lead_time_days]))
)
```

### 12. Delivered Orders
```DAX
Delivered Orders =
CALCULATE(
    DISTINCTCOUNT(Fact_Sales[order_id]),
    Fact_Sales[order_status] = "DELIVERED"
)
```

### 13. Cancelled Orders
```DAX
Cancelled Orders =
CALCULATE(
    DISTINCTCOUNT(Fact_Sales[order_id]),
    Fact_Sales[order_status] = "CANCELLED"
)
```

### 14. Refunded Orders
```DAX
Refunded Orders =
CALCULATE(
    DISTINCTCOUNT(Fact_Sales[order_id]),
    Fact_Sales[order_status] = "REFUNDED"
)
```

### 15. Cancel/Refund Rate
```DAX
Cancel/Refund Rate =
DIVIDE([Cancelled Orders] + [Refunded Orders], [Total Orders], 0)
```

### 16. Orders With Voucher
```DAX
Orders With Voucher =
CALCULATE(
    DISTINCTCOUNT(Fact_Sales[order_id]),
    Fact_Sales[is_voucher_applied] = TRUE()
)
```

### 17. Voucher Penetration
```DAX
Voucher Penetration =
DIVIDE([Orders With Voucher], [Total Orders], 0)
```

## Group 3 - Reviews and CSAT

### 18. Review Count
```DAX
Review Count = COUNTROWS(Fact_Reviews)
```

### 19. Average Rating
```DAX
Average Rating = AVERAGE(Fact_Reviews[rating])
```

## Group 4 - OPEX and profit

### 20. Total Salaries
```DAX
Total Salaries = SUM(Fact_OPEX[total_salaries])
```

### 21. Total OPEX
```DAX
Total OPEX = SUM(Fact_OPEX[total_opex])
```

### 22. Operating Profit
```DAX
Operating Profit = [Gross Profit] - [Total OPEX]
```

### 23. Labor Cost Ratio
```DAX
Labor Cost Ratio = DIVIDE([Total Salaries], [Net Revenue], 0)
```

## Group 5 - Inventory alerts

### 24. Low Stock Variants
```DAX
Low Stock Variants =
CALCULATE(
    COUNTROWS(Dim_Product),
    Dim_Product[current_stock] < 10
)
```

### 25. Low Stock Alert
```DAX
Low Stock Alert =
IF([Low Stock Variants] > 0, 1, 0)
```

## Suggested order to build visuals

1. Revenue cards
2. Profit cards
3. Orders cards
4. Review cards
5. OPEX cards
6. Low stock cards
7. Monthly line charts
8. Product/category bar charts
9. Operations tables

## Notes

- `Fact_Sales` is the main table for sales metrics
- `Fact_Reviews` is for CSAT
- `Fact_OPEX` is for cost and operating profit
- `Dim_Product` is for stock alerts and product slicing

If a measure fails, create the measures it depends on first.

