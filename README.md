# PhoneShop Platform

This repository contains two connected parts:

1. The Spring Boot e-commerce web app in `mobile/`
2. The analytics stack for BI in `dwh/`, `mockdata.py`, and `etl_to_dwh.py`

The project is split this way on purpose:
- OLTP stores the live business data for the website
- DWH stores clean analytical data for reporting and dashboards

## High-level flow

```text
Mock Data -> OLTP MySQL (`phoneshop_db`) -> ETL Python -> DWH MySQL (`phoneshop_dw`) -> Metabase
```

## Main documents

- [Final project summary](PROJECT_FINAL_SUMMARY.md)
- [Web app guide](mobile/README.md)
- [DWH and BI guide](dwh/README.md)
- [Database setup guide](DATABASE_SETUP.md)
- [Learning path](LEARNING_PATH.md)
- [Sequence diagrams chi tiết](SEQUENCE_DIAGRAMS.md)
- [Monthly helper table for Power BI](powerbi_dim_month.dax) (legacy / optional)
- [Power BI measures](powerbi_kpi_measures.dax) (legacy / optional)
- [Power BI dashboard setup](powerbi_dashboard_setup.md) (legacy / optional)
- [Metabase setup](METABASE_SETUP.md)
- [Mock data generator](mockdata.py)
- [ETL script](etl_to_dwh.py)

## What is in the web app

The Spring Boot app covers:
- login / logout / register
- product browsing and search
- cart management
- checkout and order creation
- payment flow
- product review
- customer feedback
- address management
- staff order processing
- product management
- business reports
- responsive layout and shared Thymeleaf fragments
- manager employee management
- local VNPAY mock flow for localhost testing

## What is in the BI stack

The BI stack covers:
- mock data generation for the source OLTP database
- ETL from OLTP to DWH
- star / galaxy schema for analytics
- KPI measures for Power BI
- dashboard setup for Metabase

The business report feature `U14` belongs to this stack.

## Quick start

### 1. Run the Spring Boot app

See `mobile/README.md` for the full guide.

### 2. Seed MySQL

Run `mockdata.py` after configuring MySQL connection details.

### 3. Build the DWH

Run the schema script first:

```text
dwh/phoneshop_dw_schema.sql
```

Then run the ETL:

```text
etl_to_dwh.py
```

### 4. Database options

See [Database setup guide](DATABASE_SETUP.md) for:

- MySQL setup and run order
- SQL Server porting notes for the warehouse layer

### 5. Open Metabase

Load the `phoneshop_dw` database, create the dashboard, and embed its URL through `bi.dashboard-url`.
See `METABASE_SETUP.md` for the recommended flow.

## Project status

The current system is usable for:
- OLTP testing
- order and customer workflow testing
- DWH loading
- BI dashboard building

Sprint 3 is focused on the web app polish pass:
- cleaner layout and responsive screens
- more realistic customer, staff, and manager flows
- local payment and shipment test paths
- manager-side employee management

Sprint 4 is focused on BI integration:
- Metabase dashboard setup
- iframe embedding in `/admin/reports`
- graceful fallback when BI is unavailable

If you want to continue later, the next natural steps are:
- tighten product detail, cart, and checkout UX further
- refine order timeline and admin/manager screens
- finalize Metabase dashboards and embed URL
