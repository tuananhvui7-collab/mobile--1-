"""
ETL from OLTP phoneshop_db to DWH phoneshop_dw.

Workflow:
1) Create DWH schema from dwh/phoneshop_dw_schema.sql
2) Run this script to load dimensions and facts

Install:
    pip install pymysql
"""

from __future__ import annotations

import os
from collections import defaultdict
from datetime import date, datetime, timedelta
from decimal import Decimal, ROUND_HALF_UP

import pymysql

SRC_DB = {
    "host": os.getenv("SRC_DB_HOST", "localhost"),
    "port": int(os.getenv("SRC_DB_PORT", "3306")),
    "user": os.getenv("SRC_DB_USER", "phoneshop_mock"),
    "password": os.getenv("SRC_DB_PASSWORD", "root"),
    "database": os.getenv("SRC_DB_NAME", "phoneshop_db"),
    "charset": "utf8mb4",
    "autocommit": False,
}

DWH_DB = {
    "host": os.getenv("DWH_DB_HOST", "localhost"),
    "port": int(os.getenv("DWH_DB_PORT", "3306")),
    "user": os.getenv("DWH_DB_USER", "phoneshop_mock"),
    "password": os.getenv("DWH_DB_PASSWORD", "root"),
    "database": os.getenv("DWH_DB_NAME", "phoneshop_dw"),
    "charset": "utf8mb4",
    "autocommit": False,
}

START_FALLBACK = date(2023, 1, 1)
END_FALLBACK = date(2026, 3, 31)


def money(value):
    return Decimal(str(value)).quantize(Decimal("0.01"), rounding=ROUND_HALF_UP)


def connect(cfg):
    return pymysql.connect(**cfg)


def execute_many(cur, sql, rows):
    if rows:
        cur.executemany(sql, rows)


def fetch_all_dict(cur, sql, params=None):
    cur.execute(sql, params or ())
    return cur.fetchall()


def as_date(value):
    if value is None:
        return None
    if isinstance(value, datetime):
        return value.date()
    if isinstance(value, date):
        return value
    raise TypeError(value)


def date_key(dt: date) -> int:
    return dt.year * 10000 + dt.month * 100 + dt.day


def month_key(dt: date) -> int:
    return dt.year * 100 + dt.month


def month_name_vn(m: int) -> str:
    return f"Thang {m}"


def day_name_vn(py_weekday: int) -> str:
    names = {
        0: "Thu 2",
        1: "Thu 3",
        2: "Thu 4",
        3: "Thu 5",
        4: "Thu 6",
        5: "Thu 7",
        6: "Chu nhat",
    }
    return names[py_weekday]


def truncate_target(cur):
    cur.execute("SET FOREIGN_KEY_CHECKS = 0;")
    for table in ["Fact_Sales", "Fact_Reviews", "Fact_OPEX", "Dim_Customer", "Dim_Product", "Dim_Date"]:
        cur.execute(f"TRUNCATE TABLE {table};")
    cur.execute("SET FOREIGN_KEY_CHECKS = 1;")


def load_source(cur):
    users = fetch_all_dict(cur, """
        SELECT u.user_id, u.dtype, u.email, u.full_name, u.phone, u.created_at, u.role_id, u.salary, u.hire_date, r.name_role
        FROM users u
        LEFT JOIN roles r ON r.role_id = u.role_id
    """)
    addresses = fetch_all_dict(cur, """
        SELECT address_id, user_id, city, district, ward, street, is_default, created_at
        FROM address
        ORDER BY is_default DESC, address_id ASC
    """)
    categories = fetch_all_dict(cur, """
        SELECT category_id, name, parent_id
        FROM categories
    """)
    products = fetch_all_dict(cur, """
        SELECT product_id, name, brand, description, status, created_at, category_id
        FROM products
    """)
    variants = fetch_all_dict(cur, """
        SELECT variant_id, product_id, color, storage_gb, price, stock_qty, sku, status, import_price
        FROM product_variants
    """)
    orders = fetch_all_dict(cur, """
        SELECT order_id, user_id, order_code, status, payment_method, total_amount, shipping_fee,
               discount_amount, applied_voucher, shipping_name, shipping_phone, shipping_address,
               shipping_city, created_at, updated_at
        FROM orders
    """)
    order_items = fetch_all_dict(cur, """
        SELECT order_item_id, order_id, variant_id, quantity, unit_price, product_name, variant_name, subtotal
        FROM order_items
    """)
    payments = fetch_all_dict(cur, """
        SELECT payment_id, order_id, method, status, paid_at, created_at, updated_at
        FROM payments
    """)
    shipments = fetch_all_dict(cur, """
        SELECT shipment_id, order_id, carrier, tracking_code, status, estimated_at, shipped_at, delivered_at
        FROM shipments
    """)
    reviews = fetch_all_dict(cur, """
        SELECT review_id, user_id, product_id, rating, created_at
        FROM reviews
    """)
    feedbacks = fetch_all_dict(cur, """
        SELECT feedback_id, customer_id, employee_id, status, created_at, resolved_at
        FROM feedback
    """)
    return {
        "users": users,
        "addresses": addresses,
        "categories": categories,
        "products": products,
        "variants": variants,
        "orders": orders,
        "order_items": order_items,
        "payments": payments,
        "shipments": shipments,
        "reviews": reviews,
        "feedbacks": feedbacks,
    }


def build_dim_date(src, start_dt, end_dt):
    start_day = as_date(start_dt) or START_FALLBACK
    end_day = as_date(end_dt) or END_FALLBACK
    rows = []
    d = start_day
    while d <= end_day:
        rows.append((
            date_key(d),
            d,
            d.day,
            d.month,
            month_name_vn(d.month),
            (d.month - 1) // 3 + 1,
            d.year,
            d.weekday() + 1,
            day_name_vn(d.weekday()),
            d.weekday() >= 5,
            month_key(d),
        ))
        d += timedelta(days=1)
    execute_many(src, """
        INSERT INTO Dim_Date
            (date_key, full_date, day, month, month_name, quarter, year, day_of_week, day_name, is_weekend, month_key)
        VALUES (%s, %s, %s, %s, %s, %s, %s, %s, %s, %s, %s)
    """, rows)
    return len(rows)


def build_dim_product(dwh, source):
    categories_by_id = {c["category_id"]: c for c in source["categories"]}
    products_by_id = {p["product_id"]: p for p in source["products"]}
    rows = []
    for v in source["variants"]:
        product = products_by_id[v["product_id"]]
        category = categories_by_id.get(product["category_id"])
        parent = categories_by_id.get(category["parent_id"]) if category and category["parent_id"] else None
        storage = None if v["storage_gb"] is None else str(v["storage_gb"])
        color = v["color"] or None
        variant_name = " | ".join(filter(None, [
            product["name"],
            f"{storage}GB" if storage else None,
            color,
        ]))
        if not variant_name:
            variant_name = product["name"]
        rows.append((
            v["variant_id"],
            product["name"],
            variant_name,
            category["name"] if category else "Unknown",
            parent["name"] if parent else None,
            color,
            storage,
            money(v["price"] or 0),
            money(v["import_price"] or 0),
            int(v["stock_qty"] or 0),
            v["sku"],
            v["status"],
        ))
    execute_many(dwh, """
        INSERT INTO Dim_Product
            (product_key, product_name, variant_name, category_name, parent_category, color, storage,
             current_price, import_price, current_stock, sku, status)
        VALUES (%s, %s, %s, %s, %s, %s, %s, %s, %s, %s, %s, %s)
    """, rows)
    return len(rows)


def build_dim_customer(dwh, source):
    addresses_by_user = defaultdict(list)
    for a in source["addresses"]:
        addresses_by_user[a["user_id"]].append(a)
    rows = []
    for u in source["users"]:
        if u["dtype"] != "CUSTOMER":
            continue
        addresses = addresses_by_user.get(u["user_id"], [])
        default_addr = next((a for a in addresses if bool(a["is_default"])), None)
        chosen = default_addr or (addresses[0] if addresses else None)
        rows.append((
            u["user_id"],
            u["full_name"],
            u["email"],
            u["name_role"] or u["dtype"],
            chosen["city"] if chosen else None,
            u["created_at"],
        ))
    execute_many(dwh, """
        INSERT INTO Dim_Customer
            (customer_key, full_name, email, role_name, city, registered_at)
        VALUES (%s, %s, %s, %s, %s, %s)
    """, rows)
    return len(rows)


def build_fact_sales(dwh, source):
    products_by_variant = {v["variant_id"]: v for v in source["variants"]}
    product_by_id = {p["product_id"]: p for p in source["products"]}
    orders_by_id = {o["order_id"]: o for o in source["orders"]}
    payment_latest_by_order = {}
    for p in source["payments"]:
        current = payment_latest_by_order.get(p["order_id"])
        if current is None or (p["created_at"] and current["created_at"] and p["created_at"] > current["created_at"]):
            payment_latest_by_order[p["order_id"]] = p
    shipment_by_order = {s["order_id"]: s for s in source["shipments"]}

    rows = []
    for item in source["order_items"]:
        order = orders_by_id.get(item["order_id"])
        variant = products_by_variant.get(item["variant_id"])
        if not order or not variant:
            continue
        product = product_by_id.get(variant["product_id"])
        shipment = shipment_by_order.get(order["order_id"])
        lead_time_days = None
        if shipment and shipment.get("delivered_at"):
            lead_time_days = round((shipment["delivered_at"] - order["created_at"]).total_seconds() / 86400.0, 1)
        elif shipment and shipment.get("shipped_at"):
            lead_time_days = round((shipment["shipped_at"] - order["created_at"]).total_seconds() / 86400.0, 1)
        rows.append((
            date_key(order["created_at"].date()),
            order["user_id"],
            variant["variant_id"],
            order["order_id"],
            order["status"],
            order["payment_method"],
            bool(order["applied_voucher"]),
            lead_time_days,
            int(item["quantity"]),
            money(item["unit_price"]),
            money(item["subtotal"] or (item["unit_price"] * item["quantity"])),
            money(variant["import_price"] or 0) * int(item["quantity"]),
            money(item["subtotal"] or (item["unit_price"] * item["quantity"])) - (money(variant["import_price"] or 0) * int(item["quantity"])),
            money(order["discount_amount"] or 0),
            money(order["shipping_fee"] or 0),
        ))
    execute_many(dwh, """
        INSERT INTO Fact_Sales
            (date_key, customer_key, product_key, order_id, order_status, payment_method,
             is_voucher_applied, lead_time_days, quantity, unit_price, revenue, cogs, gross_profit,
             discount_amount, shipping_fee)
        VALUES (%s, %s, %s, %s, %s, %s, %s, %s, %s, %s, %s, %s, %s, %s, %s)
    """, rows)
    return len(rows)


def build_fact_reviews(dwh, source):
    rows = []
    orders_by_customer = defaultdict(list)
    for o in source["orders"]:
        orders_by_customer[o["user_id"]].append(o)
    for r in source["reviews"]:
        order = next((o for o in orders_by_customer.get(r["user_id"], []) if o["created_at"].date() <= r["created_at"].date()), None)
        if order is None:
            continue
        rows.append((
            date_key(r["created_at"].date()),
            r["user_id"],
            r["product_id"],
            order["order_id"],
            int(r["rating"]),
        ))
    execute_many(dwh, """
        INSERT INTO Fact_Reviews
            (date_key, customer_key, product_key, order_id, rating)
        VALUES (%s, %s, %s, %s, %s)
    """, rows)
    return len(rows)


def build_fact_opex(dwh, source):
    salaries_by_month = defaultdict(Decimal)
    employees = [u for u in source["users"] if u["dtype"] in {"EMPLOYEE", "MANAGER"}]
    if not employees:
        return 0

    min_dt = min((u["hire_date"] or u["created_at"]) for u in employees).date()
    max_dt = max((o["created_at"] for o in source["orders"]), default=datetime.combine(END_FALLBACK, datetime.min.time())).date()
    start = date(min_dt.year, min_dt.month, 1)
    end = date(max_dt.year, max_dt.month, 1)

    for emp in employees:
        salary = money(emp["salary"] or 0)
        hire = (emp["hire_date"] or emp["created_at"]).date()
        d = date(hire.year, hire.month, 1)
        while d <= end:
            salaries_by_month[month_key(d)] += salary
            if d.month == 12:
                d = date(d.year + 1, 1, 1)
            else:
                d = date(d.year, d.month + 1, 1)

    rows = []
    d = start
    while d <= end:
        mk = month_key(d)
        total_salaries = money(salaries_by_month.get(mk, Decimal("0")))
        rows.append((mk, d.year, d.month, total_salaries, money(0), total_salaries))
        if d.month == 12:
            d = date(d.year + 1, 1, 1)
        else:
            d = date(d.year, d.month + 1, 1)

    execute_many(dwh, """
        INSERT INTO Fact_OPEX
            (month_key, year, month, total_salaries, other_opex, total_opex)
        VALUES (%s, %s, %s, %s, %s, %s)
    """, rows)
    return len(rows)


def main():
    src = connect(SRC_DB)
    dwh = connect(DWH_DB)
    try:
        src_cur = src.cursor(pymysql.cursors.DictCursor)
        dwh_cur = dwh.cursor()

        print("Loading source data...")
        source = load_source(src_cur)

        print("Refreshing DWH...")
        truncate_target(dwh_cur)

        order_dates = [o["created_at"] for o in source["orders"] if o["created_at"]]
        review_dates = [r["created_at"] for r in source["reviews"] if r["created_at"]]
        all_dates = order_dates + review_dates
        start_dt = min(all_dates) if all_dates else datetime.combine(START_FALLBACK, datetime.min.time())
        end_dt = max(all_dates) if all_dates else datetime.combine(END_FALLBACK, datetime.min.time())

        dim_date_count = build_dim_date(dwh_cur, start_dt, end_dt)
        dim_product_count = build_dim_product(dwh_cur, source)
        dim_customer_count = build_dim_customer(dwh_cur, source)
        fact_sales_count = build_fact_sales(dwh_cur, source)
        fact_reviews_count = build_fact_reviews(dwh_cur, source)
        fact_opex_count = build_fact_opex(dwh_cur, source)

        dwh.commit()
        print("Done.")
        print(f"Dim_Date: {dim_date_count}")
        print(f"Dim_Product: {dim_product_count}")
        print(f"Dim_Customer: {dim_customer_count}")
        print(f"Fact_Sales: {fact_sales_count}")
        print(f"Fact_Reviews: {fact_reviews_count}")
        print(f"Fact_OPEX: {fact_opex_count}")
    finally:
        src.close()
        dwh.close()


if __name__ == "__main__":
    main()
