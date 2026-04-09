# Database Setup Guide

This project currently runs end-to-end on **MySQL**.
The **SQL Server** notes below are included as a porting guide for the warehouse layer, not as a fully wired one-click path.

## 1) MySQL setup

This is the recommended and fully supported path for the current codebase.

### What you need

- MySQL 8.x
- MySQL Workbench or another SQL client
- Python 3.11+
- `pymysql` installed for ETL

### Databases

Create these databases:

- `phoneshop_db` for OLTP
- `phoneshop_dw` for DWH

### Run order

1. Create the DWH schema:

```text
dwh/phoneshop_dw_schema.sql
```

2. Seed the OLTP database:

```text
mockdata.py
```

3. Run the ETL:

```text
etl_to_dwh.py
```

4. Open Power BI or Metabase and connect to `phoneshop_dw`.

### Useful environment variables

If your MySQL user or password is different, set these before running ETL:

```powershell
$env:SRC_DB_HOST="localhost"
$env:SRC_DB_PORT="3306"
$env:SRC_DB_USER="phoneshop_mock"
$env:SRC_DB_PASSWORD="root"
$env:SRC_DB_NAME="phoneshop_db"

$env:DWH_DB_HOST="localhost"
$env:DWH_DB_PORT="3306"
$env:DWH_DB_USER="phoneshop_mock"
$env:DWH_DB_PASSWORD="root"
$env:DWH_DB_NAME="phoneshop_dw"
```

### Notes

- `mockdata.py` seeds the OLTP database.
- `etl_to_dwh.py` reads from MySQL and writes to MySQL.
- The current warehouse schema and ETL script are written for MySQL syntax and MySQL connectors.

---

## 2) SQL Server setup

This section is a **porting guide** if you want to move the warehouse layer to Microsoft SQL Server.
It is not the default path in the repo today.

### What you need

- SQL Server Express / Developer
- SQL Server Management Studio (SSMS)
- Python 3.11+
- `pyodbc` or `sqlalchemy` if you build a SQL Server ETL variant

### What stays the same

- `phoneshop_db` can still remain on MySQL as the OLTP source.
- `mockdata.py` still seeds the source database.
- The business logic in the web app does not need to change just because the warehouse target changes.

### What must change for SQL Server

If you want the warehouse in SQL Server, you need a SQL Server-compatible version of:

- `dwh/phoneshop_dw_schema.sql`
- `etl_to_dwh.py`

The current ETL script uses `pymysql`, so it will not write to SQL Server as-is.

### SQL Server schema conversion notes

When porting the warehouse DDL from MySQL to SQL Server:

- `AUTO_INCREMENT` becomes `IDENTITY(1,1)`
- `BOOLEAN` becomes `BIT`
- `utf8mb4` charset / collation clauses are removed or replaced with SQL Server collation settings
- `VARCHAR`, `INT`, `BIGINT`, `DECIMAL`, `DATE`, `DATETIME` map mostly directly
- foreign keys and indexes should be recreated in T-SQL syntax

### SQL Server ETL notes

To load data into SQL Server, rewrite the ETL connection layer using one of:

- `pyodbc`
- `sqlalchemy` with a SQL Server driver

You would then:

1. Read from MySQL `phoneshop_db`
2. Transform data in Python
3. Insert into SQL Server `phoneshop_dw`

### Recommended rule

If your goal is to finish the current project quickly, stay on MySQL for both OLTP and DWH.
If your goal is to demonstrate SQL Server specifically, port the DWH layer first, then switch the ETL connector.

