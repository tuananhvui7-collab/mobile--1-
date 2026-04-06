# PhoneShop DWH and BI

This folder contains the analytical database for the project.

## Purpose

The operational database (`phoneshop_db`) is good for running the shop.
The warehouse database (`phoneshop_dw`) is good for reporting, dashboards, and KPI analysis.

In this project, the business use case `U14: Xem báo cáo kinh doanh` maps directly to this BI stack.
So the reporting feature in the web app and the warehouse dashboard are part of the same requirement.

## Data flow

```text
mockdata.py -> phoneshop_db -> etl_to_dwh.py -> phoneshop_dw -> Power BI / Metabase
```

## Warehouse schema

The warehouse uses a small galaxy-style model:

- `Dim_Date`
- `Dim_Product`
- `Dim_Customer`
- `Fact_Sales`
- `Fact_Reviews`
- `Fact_OPEX`

## Files

- `phoneshop_dw_schema.sql`
  - creates the warehouse tables
- `load_phoneshop_dw.sql`
  - optional SQL load script
- `etl_to_dwh.py`
  - Python ETL from OLTP to DWH
- `powerbi_dim_month.dax`
  - calculated table for monthly OPEX visuals, built from `Fact_OPEX`
- `powerbi_kpi_measures.dax`
  - DAX measures for Power BI
- `powerbi_dashboard_setup.md`
  - step-by-step dashboard layout

## What each table means

### Dim_Date
One row per date.
Use it for:
- year
- quarter
- month
- weekday
- time-based slicing

### Dim_Product
One row per product variant.
Use it for:
- product name
- category
- color
- storage
- price
- stock

### Dim_Customer
One row per customer.
Use it for:
- customer identity
- city
- role

### Fact_Sales
One row per order item.
Use it for:
- revenue
- quantity sold
- COGS
- gross profit
- order status
- payment method
- discount
- shipping fee
- lead time

### Fact_Reviews
One row per review.
Use it for:
- rating analysis
- product CSAT

### Fact_OPEX
One row per month.
Use it for:
- salary cost
- operating profit
- labor cost ratio

## KPI coverage

The DWH is designed to support these KPIs:

- Net Revenue
- Gross Profit
- Gross Margin %
- Operating Profit
- Labor Cost Ratio
- AOV
- Lead Time
- Cancel / Refund Rate
- Voucher Penetration
- Total Units Sold
- Low-Stock Alert
- Average Rating

## How to build it

### 1. Create the warehouse

Run:

```text
phoneshop_dw_schema.sql
```

### 2. Seed the source database

Run:

```text
mockdata.py
```

This fills `phoneshop_db`.

### 3. Run ETL

Run:

```text
etl_to_dwh.py
```

This loads data from `phoneshop_db` into `phoneshop_dw`.

### 4. Build the dashboard

Open Power BI Desktop and follow:

```text
powerbi_dashboard_setup.md
```

Create the monthly helper table from:

```text
powerbi_dim_month.dax
```

Then paste the measures from:

```text
powerbi_kpi_measures.dax
```

## Power BI relationships

Create these relationships:

- `Dim_Date[date_key]` -> `Fact_Sales[date_key]`
- `Dim_Date[date_key]` -> `Fact_Reviews[date_key]`
- `Dim_Customer[customer_key]` -> `Fact_Sales[customer_key]`
- `Dim_Customer[customer_key]` -> `Fact_Reviews[customer_key]`
- `Dim_Product[product_key]` -> `Fact_Sales[product_key]`
- `Dim_Product[product_key]` -> `Fact_Reviews[product_key]`
- `Dim_Month[month_key]` -> `Fact_OPEX[month_key]`

## Important modeling rule

`Fact_Sales` is at order-item grain.
That means:
- do not sum order-level values naively across line items
- use DAX carefully for `discount_amount`, `shipping_fee`, and `lead_time`

This is why the DAX file uses `SUMX(VALUES(Fact_Sales[order_id]), ...)` for some measures.

## Recommended dashboard pages

1. Executive Overview
2. Sales Performance
3. Operations
4. Customer and CSAT
5. Inventory Risk

## What to do first in Power BI

1. Connect to MySQL `phoneshop_dw`
2. Load the tables
3. Create the relationships
4. Create the DAX measures
5. Build the visuals

## If the fact tables are empty

That means the OLTP source does not yet have enough transactional data.
In that case:
- run `mockdata.py`
- rerun `etl_to_dwh.py`

## Learning shortcut

If you are new to BI, remember this order:

1. source data
2. ETL
3. warehouse
4. DAX
5. visuals

That is the full path from raw data to dashboard.
