# PhoneShop Platform

This repository contains two connected parts:

1. The Spring Boot e-commerce web app in `mobile/`
2. The analytics stack for BI in `dwh/`, `mockdata.py`, and `etl_to_dwh.py`

The project is split this way on purpose:
- OLTP stores the live business data for the website
- DWH stores clean analytical data for reporting and dashboards

## High-level flow

```text
Mock Data -> OLTP MySQL (`phoneshop_db`) -> ETL Python -> DWH MySQL (`phoneshop_dw`) -> Power BI / Metabase
```

## Main documents

- [Web app guide](mobile/README.md)
- [DWH and BI guide](dwh/README.md)
- [Learning path](LEARNING_PATH.md)
- [Monthly helper table for Power BI](powerbi_dim_month.dax)
- [Power BI measures](powerbi_kpi_measures.dax)
- [Power BI dashboard setup](powerbi_dashboard_setup.md)
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

## What is in the BI stack

The BI stack covers:
- mock data generation for the source OLTP database
- ETL from OLTP to DWH
- star / galaxy schema for analytics
- KPI measures for Power BI
- dashboard setup for Power BI or Metabase

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

### 4. Open Power BI

Load the `phoneshop_dw` database, create the relationships, and paste the measures from `powerbi_kpi_measures.dax`.

## Project status

The current system is usable for:
- OLTP testing
- order and customer workflow testing
- DWH loading
- BI dashboard building

If you want to continue later, the next natural steps are:
- improve the UI templates
- harden the shipment tracking flow
- add more dashboard pages
- refine the DWH measures
