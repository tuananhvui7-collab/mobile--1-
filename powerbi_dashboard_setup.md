# Power BI Dashboard Setup - phoneshop_dw

## 1) Load tables
Connect Power BI Desktop to MySQL database `phoneshop_dw` and load:
- `Dim_Date`
- `Dim_Product`
- `Dim_Customer`
- `Fact_Sales`
- `Fact_Reviews`
- `Fact_OPEX`

Create `Dim_Month` as a separate calculated table using:

- `powerbi_dim_month.dax`


If you do not want to create `Dim_Month`, you can still use `Fact_OPEX` in cards and tables, but month-by-month charts will be harder to build cleanly.

## 2) Relationships
Create these relationships:
- `Dim_Date[date_key]` -> `Fact_Sales[date_key]`
- `Dim_Date[date_key]` -> `Fact_Reviews[date_key]`
- `Dim_Customer[customer_key]` -> `Fact_Sales[customer_key]`
- `Dim_Customer[customer_key]` -> `Fact_Reviews[customer_key]`
- `Dim_Product[product_key]` -> `Fact_Sales[product_key]`
- `Dim_Product[product_key]` -> `Fact_Reviews[product_key]`
- `Dim_Month[month_key]` -> `Fact_OPEX[month_key]`

Use:
- cardinality: `One to many`
- cross filter direction: `Single`

## 3) Pages to create

### Page 1 - Executive Overview
Put these visuals:
- Card: `Net Revenue`
- Card: `Gross Profit`
- Card: `Gross Margin %`
- Card: `Operating Profit`
- Card: `AOV`
- Card: `Average Rating`
- Line chart: revenue by month
- Bar chart: revenue by category
- Card or KPI: `Low Stock Alert`

### Page 2 - Sales Performance
Put these visuals:
- Line chart: `Net Revenue` by date
- Bar chart: `Net Revenue` by product
- Matrix: product / quantity / revenue / gross profit
- Slicers: year, month, category, brand

### Page 3 - Operations
Put these visuals:
- Card: `Total Orders`
- Card: `Total Units Sold`
- Card: `Cancel/Refund Rate`
- Card: `Voucher Penetration`
- Card: `Average Lead Time (Days)`
- Bar chart: orders by status
- Table: order id, status, payment method, lead time
- Card: `Total Shipping Fee`

### Page 4 - Customer & CSAT
Put these visuals:
- Card: `Review Count`
- Card: `Average Rating`
- Bar chart: rating by product
- Table: top reviewed products
- Slicers: city, category, role

### Page 5 - Inventory / Risk
Put these visuals:
- Card: `Low Stock Variants`
- Table: products with current stock < 10
- Bar chart: stock by product

## 4) KPI definitions used
- `Net Revenue` = gross revenue minus discounts
- `Gross Profit` = net revenue minus COGS
- `Gross Margin %` = gross profit / net revenue
- `Operating Profit` = gross profit - total OPEX
- `Labor Cost Ratio` = total salaries / net revenue
- `AOV` = net revenue / total orders
- `Cancel/Refund Rate` = cancelled + refunded orders / total orders
- `Voucher Penetration` = orders with voucher / total orders
- `Average Lead Time (Days)` = average shipment lead time per order
- `Average Rating` = average review score

## 5) Sorting tip
For month visuals:
- sort `month_label` by `month_sort`

For category and product charts:
- sort by `Net Revenue` descending

## 6) Measure creation order

Use `powerbi_kpi_measures_step_by_step.md` and create the measures in that order.
