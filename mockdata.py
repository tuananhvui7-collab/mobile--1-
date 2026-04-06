"""
Mock data generator for the current PhoneShop schema.

Use this script separately from Spring Boot's DataSeederConfig:
- DataSeederConfig: internal manager/employee bootstrap
- mockdata.py: bulk OLTP fake data for BI / warehouse prep

Install:
    pip install faker mysql-connector-python pymysql bcrypt

Run:
    python mockdata.py
"""

from __future__ import annotations

import json
import os
import random
import re
from collections import defaultdict
from datetime import date, datetime, timedelta
from decimal import Decimal, ROUND_HALF_UP

from faker import Faker

try:
    import pymysql
except ImportError:  # pragma: no cover - optional fallback
    pymysql = None

try:
    import mysql.connector as mysql_connector
except ImportError:  # pragma: no cover - optional fallback
    mysql_connector = None

DB_CONFIG = {
    "host": "localhost",
    "port": 3306,
    "user": "phoneshop_mock",
    "password": "root",
    "database": "phoneshop_db",
    "charset": "utf8mb4",
}

# Prefer PyMySQL because it is less sensitive to auth-plugin issues on MySQL 8.
USE_PYMYSQL = True


def connect_db():
    if USE_PYMYSQL:
        if pymysql is None:
            raise RuntimeError(
                "PyMySQL is not installed. Run: pip install pymysql\n"
                "This script now prefers PyMySQL to avoid MySQL auth plugin issues."
            )
        conn = pymysql.connect(
            host=DB_CONFIG["host"],
            port=DB_CONFIG["port"],
            user=DB_CONFIG["user"],
            password=DB_CONFIG["password"],
            database=DB_CONFIG["database"],
            charset=DB_CONFIG["charset"],
            autocommit=False,
        )
        return conn

    if mysql_connector is None:
        raise RuntimeError(
            "mysql-connector-python is not installed. Run: pip install mysql-connector-python"
        )

    conn = mysql_connector.connect(
        **DB_CONFIG,
        auth_plugin="mysql_native_password",
    )
    conn.autocommit = False
    return conn


NUM_STAFF = int(os.getenv("NUM_STAFF", "10"))
NUM_CUSTOMERS = int(os.getenv("NUM_CUSTOMERS", "400"))
NUM_ORDERS = int(os.getenv("NUM_ORDERS", "1500"))
NUM_FEEDBACK = int(os.getenv("NUM_FEEDBACK", "180"))

DATA_START_DATE = date(2023, 1, 1)
DATA_END_DATE = date(2026, 3, 31)
DEFAULT_PASSWORD = "123456"
PASSWORD_HASH = "$2b$10$CpZFBoCT1/M8soplg.8lkOhAy00zwzdqdWKQ/2qhLqLqGnlX0DRTW"

fake = Faker("vi_VN")
Faker.seed(42)
random.seed(42)

ROLE_ROWS = [(1, "MANAGER"), (2, "EMPLOYEE"), (3, "CUSTOMER")]
CATEGORY_ROWS = [
    (1, "Điện thoại", "dien-thoai", True, None),
    (2, "Tablet", "tablet", True, None),
    (3, "Phụ kiện", "phu-kien", True, None),
    (4, "Apple", "apple", True, 1),
    (5, "Samsung", "samsung", True, 1),
    (6, "Xiaomi", "xiaomi", True, 1),
    (7, "OPPO", "oppo", True, 1),
    (8, "Vivo", "vivo", True, 1),
    (9, "iPad", "ipad", True, 2),
    (10, "Tai nghe", "tai-nghe", True, 3),
    (11, "Sạc & Cáp", "sac-cap", True, 3),
    (12, "Ốp lưng", "op-lung", True, 3),
]


def money(v):
    return Decimal(str(v)).quantize(Decimal("0.01"), rounding=ROUND_HALF_UP)


def rand_dt(start, end):
    days = max((end - start).days, 0)
    base = start + timedelta(days=random.randint(0, days))
    return datetime.combine(base, datetime.min.time()) + timedelta(
        hours=random.randint(8, 22), minutes=random.randint(0, 59), seconds=random.randint(0, 59)
    )


def rand_phone():
    return "0" + "".join(random.choice("0123456789") for _ in range(9))


def slugify(text):
    return re.sub(r"[^a-z0-9]+", "-", text.lower()).strip("-")


def execute_many(cur, sql, rows):
    if rows:
        cur.executemany(sql, rows)


def get_existing_tables(cur):
    cur.execute(
        """
        SELECT table_name
        FROM information_schema.tables
        WHERE table_schema = %s
        """,
        (DB_CONFIG["database"],),
    )
    return {row[0] for row in cur.fetchall()}


def get_table_columns(cur, table_name):
    cur.execute(
        """
        SELECT column_name
        FROM information_schema.columns
        WHERE table_schema = %s AND table_name = %s
        ORDER BY ordinal_position
        """,
        (DB_CONFIG["database"], table_name),
    )
    return [row[0] for row in cur.fetchall()]


def truncate_tables(cur, existing_tables):
    tables = [
        "shipment_events", "shipments", "payments", "order_items", "orders",
        "cart_items", "carts", "reviews", "feedback", "product_images",
        "product_variants", "products", "address", "users", "categories", "roles",
    ]
    cur.execute("SET FOREIGN_KEY_CHECKS = 0;")
    skipped = []
    for tbl in tables:
        if tbl in existing_tables:
            cur.execute(f"TRUNCATE TABLE {tbl};")
        else:
            skipped.append(tbl)
    cur.execute("SET FOREIGN_KEY_CHECKS = 1;")
    return skipped


def random_location():
    locations = [
        ("Ho Chi Minh", "District 1", "Ben Nghe"),
        ("Ho Chi Minh", "District 3", "Ward 6"),
        ("Ho Chi Minh", "Binh Thanh", "Ward 12"),
        ("Ho Chi Minh", "Thu Duc", "An Phu"),
        ("Ha Noi", "Ba Dinh", "Ngoc Ha"),
        ("Ha Noi", "Cau Giay", "Dich Vong"),
        ("Ha Noi", "Dong Da", "Kim Lien"),
        ("Da Nang", "Hai Chau", "Thach Thang"),
        ("Can Tho", "Ninh Kieu", "An Cu"),
        ("Hai Phong", "Hong Bang", "Minh Khai"),
        ("Binh Duong", "Thu Dau Mot", "Phu Cuong"),
        ("Dong Nai", "Bien Hoa", "Tan Phong"),
        ("Hue", "Phu Xuan", "Phu Hoi"),
        ("Nha Trang", "Nha Trang", "Loc Tho"),
        ("Vung Tau", "Vung Tau", "Ward 1"),
    ]
    return random.choice(locations)


def weighted_choice(items):
    values = [x[0] for x in items]
    weights = [x[1] for x in items]
    return random.choices(values, weights=weights, k=1)[0]


def voucher_discount(subtotal):
    pool = [
        ("WELCOME10", "percent", 10, 1000000),
        ("TECH15", "percent", 15, 1500000),
        ("FLAT50K", "fixed", 50000, 50000),
        ("FLASH20", "percent", 20, 800000),
        ("VIP5", "percent", 5, 300000),
    ]
    if random.random() > 0.28:
        return None, money(0)
    code, kind, value, cap = random.choice(pool)
    if kind == "percent":
        discount = money((subtotal * Decimal(value)) / Decimal("100"))
        discount = min(discount, money(cap))
    else:
        discount = money(value)
    return code, discount


def payment_status_for(order_status, method):
    if order_status == "DELIVERED":
        return "SUCCESS"
    if order_status in {"CONFIRMED", "PACKING", "SHIPPING"}:
        return "SUCCESS" if method == "VN_PAY" else "PENDING"
    if order_status == "PENDING":
        return random.choice(["PENDING", "FAILED"])
    return random.choice(["FAILED", "REFUNDED"])


def shipment_status_for(order_status):
    return {
        "CONFIRMED": "PENDING",
        "PACKING": "PICKED_UP",
        "SHIPPING": "IN_TRANSIT",
        "DELIVERED": "DELIVERED",
        "CANCELLED": "CANCELLED",
    }.get(order_status)

PHONE_PRODUCTS = [
    ("iPhone 15", "Apple", 4, "Apple iPhone 15 với chip A16, camera ổn định và pin tốt.", "IP15", 21990000, 3500000, [128, 256, 512], ["BLACK", "BLUE", "NAT"], 0.80),
    ("iPhone 15 Pro", "Apple", 4, "iPhone 15 Pro với chip A17 Pro, vật liệu cao cấp.", "IP15P", 28990000, 4000000, [128, 256, 512], ["TITAN-BLACK", "TITAN-WHITE", "TITAN-NAT"], 0.82),
    ("Samsung Galaxy S24", "Samsung", 5, "Galaxy S24 với AI, camera mạnh và màn hình sáng.", "S24", 22990000, 3000000, [256, 512], ["ONYX", "MARBLE"], 0.79),
    ("Samsung Galaxy A55", "Samsung", 5, "Galaxy A55 tầm trung cao cấp, pin tốt, camera ổn.", "A55", 10990000, 1800000, [128, 256], ["NAVY", "LILAC"], 0.76),
    ("Xiaomi 14", "Xiaomi", 6, "Xiaomi 14 hiệu năng mạnh, sạc nhanh, camera Leica.", "XM14", 19990000, 3000000, [256, 512], ["BLACK", "WHITE"], 0.79),
    ("Redmi Note 13", "Xiaomi", 6, "Redmi Note 13 cân bằng giữa pin, màn hình và giá.", "RN13", 5990000, 1200000, [128, 256], ["BLACK", "GREEN"], 0.74),
    ("OPPO Reno12", "OPPO", 7, "OPPO Reno12 nổi bật với camera và thiết kế mỏng nhẹ.", "R12", 10990000, 1500000, [256], ["GOLD"], 0.75),
    ("Vivo V30", "Vivo", 8, "Vivo V30 chú trọng selfie, mỏng nhẹ và pin ổn.", "V30", 10990000, 1500000, [256], ["PEARL"], 0.74),
    ("iPad Pro M4", "Apple", 9, "iPad Pro M4 phục vụ sáng tạo nội dung và công việc nặng.", "IPDM4", 27990000, 4500000, [256, 512], ["SILVER", "SPACEGRAY"], 0.81),
    ("iPad Air M2", "Apple", 9, "iPad Air M2 gọn nhẹ, phù hợp học tập và làm việc.", "IPAIR", 16990000, 2500000, [128, 256], ["BLUE", "PURPLE"], 0.79),
    ("AirPods Pro 2", "Apple", 10, "Tai nghe true wireless chống ồn, tối ưu cho hệ sinh thái Apple.", "APP2", 5790000, 0, [None, None], ["WHITE", "BLACK"], 0.78),
    ("Tai nghe TWS ANC", "PhoneShop", 10, "Tai nghe TWS chống ồn chủ động, pin tốt, giá mềm.", "TWSANC", 1290000, 0, [None, None, None], ["WHITE", "BLACK", "BLUE"], 0.72),
    ("Cáp sạc Type-C 1m", "Baseus", 11, "Cáp sạc Type-C bền, phù hợp sạc nhanh.", "CABLE1M", 129000, 20000, [None, None, None], ["1M", "2M", "3M"], 0.70),
    ("Sạc nhanh 67W", "Xiaomi", 11, "Củ sạc nhanh 67W cho điện thoại và tablet.", "CHG67W", 399000, 40000, [None, None], ["WHITE", "BLACK"], 0.71),
    ("Ốp lưng iPhone 15", "PhoneShop", 12, "Ốp lưng silicon bảo vệ tốt, nhiều màu.", "CASEIP15", 99000, 10000, [None, None, None], ["CLEAR", "BLACK", "PINK"], 0.65),
]


def seed_roles(cur):
    execute_many(cur, "INSERT INTO roles (role_id, name_role) VALUES (%s, %s)", ROLE_ROWS)


def seed_users(cur):
    rows = []
    customers, employees, managers = [], [], []
    uid = 1

    def add_user(dtype, role_id, email, full_name, phone, created_at, salary=None, hire_date=None):
        nonlocal uid
        row = {
            "user_id": uid,
            "dtype": dtype,
            "email": email,
            "password_hash": PASSWORD_HASH,
            "full_name": full_name,
            "phone": phone,
            "is_active": True,
            "created_at": created_at,
            "role_id": role_id,
            "salary": salary,
            "hire_date": hire_date,
        }
        rows.append((uid, dtype, email, PASSWORD_HASH, full_name, phone, True, created_at, role_id, salary, hire_date))
        uid += 1
        return row

    managers.append(add_user("MANAGER", 1, "manager1@gmail.com", "Manager 1", rand_phone(), rand_dt(DATA_START_DATE, DATA_START_DATE + timedelta(days=30)), money(20000000), rand_dt(DATA_START_DATE, DATA_START_DATE + timedelta(days=30))))
    for idx in range(NUM_STAFF):
        employees.append(add_user("EMPLOYEE", 2, "employee1@gmail.com" if idx == 0 else f"employee{idx + 1:02d}@phoneshop.vn", fake.name(), rand_phone(), rand_dt(DATA_START_DATE, DATA_START_DATE + timedelta(days=60)), money(random.randint(8000000, 15000000)), rand_dt(DATA_START_DATE, DATA_START_DATE + timedelta(days=90))))
    for idx in range(NUM_CUSTOMERS):
        customers.append(add_user("CUSTOMER", 3, f"customer{idx + 1:04d}@phoneshop.vn", fake.name(), rand_phone(), rand_dt(DATA_START_DATE, DATA_END_DATE - timedelta(days=15))))

    execute_many(cur, """
        INSERT INTO users
            (user_id, dtype, email, password_hash, full_name, phone, is_active, created_at, role_id, salary, hire_date)
        VALUES (%s, %s, %s, %s, %s, %s, %s, %s, %s, %s, %s)
    """, rows)
    return customers, employees, managers


def seed_addresses(cur, customers):
    rows = []
    amap = defaultdict(list)
    aid = 1
    for customer in customers:
        count = 1 if random.random() < 0.65 else 2
        if random.random() < 0.12:
            count += 1
        default_idx = random.randint(0, count - 1)
        for i in range(count):
            city, district, ward = random_location()
            row = {
                "address_id": aid,
                "user_id": customer["user_id"],
                "street": fake.street_address(),
                "ward": ward,
                "phone": rand_phone(),
                "district": district,
                "city": city,
                "is_default": i == default_idx,
                "created_at": rand_dt(DATA_START_DATE, DATA_END_DATE),
            }
            rows.append((aid, customer["user_id"], row["street"], ward, row["phone"], district, city, row["is_default"], row["created_at"]))
            amap[customer["user_id"]].append(row)
            aid += 1
    execute_many(cur, """
        INSERT INTO address
            (address_id, user_id, street, ward, phone, district, city, is_default, created_at)
        VALUES (%s, %s, %s, %s, %s, %s, %s, %s, %s)
    """, rows)
    return rows, amap


def seed_categories(cur):
    execute_many(cur, "INSERT INTO categories (category_id, name, slug, is_active, parent_id) VALUES (%s, %s, %s, %s, %s)", CATEGORY_ROWS)


def seed_products(cur):
    prows, vrows, irows = [], [], []
    products, variants, images = [], [], []
    pid = vid = iid = 1
    for name, brand, cat_id, desc, prefix, base_price, step, storages, suffixes, ratio in PHONE_PRODUCTS:
        created_at = rand_dt(DATA_START_DATE, DATA_END_DATE - timedelta(days=30))
        products.append({"product_id": pid, "name": name, "brand": brand, "description": desc, "status": "ACTIVE", "created_at": created_at, "category_id": cat_id})
        prows.append((pid, name, brand, desc, "ACTIVE", created_at, cat_id))
        for idx, storage in enumerate(storages):
            suffix = suffixes[idx % len(suffixes)]
            price = money(base_price + (step * idx))
            import_price = money(price * Decimal(str(ratio)))
            sku = f"{prefix}-{storage if storage is not None else 'NA'}-{suffix}"
            stock_qty = random.randint(0, 220)
            variants.append({"variant_id": vid, "storage_gb": storage, "price": price, "import_price": import_price, "stock_qty": stock_qty, "sku": sku, "product_id": pid, "product_name": name})
            vrows.append((vid, storage, price, import_price, stock_qty, sku, pid))
            for img_idx in range(random.randint(2, 4)):
                url = f"https://picsum.photos/seed/{slugify(sku)}-{img_idx + 1}/900/900"
                images.append({"image_id": iid, "url": url, "is_primary": img_idx == 0, "variant_id": vid})
                irows.append((iid, url, img_idx == 0, vid))
                iid += 1
            vid += 1
        pid += 1

    execute_many(cur, "INSERT INTO products (product_id, name, brand, description, status, created_at, category_id) VALUES (%s, %s, %s, %s, %s, %s, %s)", prows)
    execute_many(cur, "INSERT INTO product_variants (variant_id, storage_gb, price, import_price, stock_qty, sku, product_id) VALUES (%s, %s, %s, %s, %s, %s, %s)", vrows)
    execute_many(cur, "INSERT INTO product_images (image_id, url, is_primary, variant_id) VALUES (%s, %s, %s, %s)", irows)
    return products, variants, images

PAYMENT_METHODS = [("COD", 0.48), ("VN_PAY", 0.52)]
ORDER_STATUS_WEIGHTS = [("PENDING", 0.08), ("CONFIRMED", 0.12), ("PACKING", 0.12), ("SHIPPING", 0.18), ("DELIVERED", 0.42), ("CANCELLED", 0.08)]


def build_shipment_events(order_status, order_created_at):
    seq = {
        "CONFIRMED": [("PENDING", "ready_to_pick", "created", "Đơn đã được tạo")],
        "PACKING": [("PENDING", "ready_to_pick", "created", "Đơn đã được tạo"), ("PICKED_UP", "picking", "picked_up", "GHN đã lấy hàng")],
        "SHIPPING": [("PENDING", "ready_to_pick", "created", "Đơn đã được tạo"), ("PICKED_UP", "picking", "picked_up", "GHN đã lấy hàng"), ("IN_TRANSIT", "transporting", "in_transit", "Kiện hàng đang trên đường vận chuyển")],
        "DELIVERED": [("PENDING", "ready_to_pick", "created", "Đơn đã được tạo"), ("PICKED_UP", "picking", "picked_up", "GHN đã lấy hàng"), ("IN_TRANSIT", "transporting", "in_transit", "Kiện hàng đang trên đường vận chuyển"), ("DELIVERED", "delivered", "delivered", "Kiện hàng đã giao thành công")],
        "CANCELLED": [("PENDING", "ready_to_pick", "created", "Đơn đã được tạo"), ("CANCELLED", "cancelled", "cancelled", "Đơn đã bị hủy")],
    }.get(order_status, [("PENDING", "ready_to_pick", "created", "Đơn đã được tạo")])
    hubs = ["HCM Central Hub", "Ha Noi North Hub", "Da Nang Mid Hub", "Bien Hoa Sort Center"]
    out = []
    for idx, (sstatus, ghn_status, etype, desc) in enumerate(seq):
        occurred_at = order_created_at + timedelta(days=1, hours=idx * 6 + random.randint(0, 3))
        out.append({
            "shipment_status": sstatus,
            "ghn_status": ghn_status,
            "event_type": etype,
            "warehouse": random.choice(hubs),
            "description": desc,
            "occurred_at": occurred_at,
            "raw_payload": json.dumps({"shipment_status": sstatus, "ghn_status": ghn_status, "event_type": etype}, ensure_ascii=False),
        })
    return out


def seed_carts(cur, customers, variants):
    carts, cart_items = [], []
    crows, irows = [], []
    cid = ciid = 1
    cart_columns = get_table_columns(cur, "carts")
    cart_item_columns = get_table_columns(cur, "cart_items")
    cart_user_column = "user_id" if "user_id" in cart_columns else ("customer_id" if "customer_id" in cart_columns else None)
    if cart_user_column is None:
        raise RuntimeError(
            "Could not find a customer foreign key column in carts table. "
            "Expected either 'user_id' or 'customer_id'."
        )
    for customer in customers:
        if random.random() > 0.72:
            continue
        created_at = rand_dt(DATA_START_DATE, DATA_END_DATE)
        carts.append({"cart_id": cid, "user_id": customer["user_id"], "created_at": created_at, "updated_at": created_at + timedelta(minutes=random.randint(1, 300))})
        crows.append((cid, customer["user_id"], created_at, created_at + timedelta(minutes=random.randint(1, 300))))
        picked = random.sample(variants, k=random.randint(1, min(5, len(variants))))
        for v in picked:
            qty = random.randint(1, 3)
            added_at = created_at + timedelta(minutes=random.randint(1, 180))
            subtotal = money(v["price"] * qty)
            cart_items.append({"cart_item_id": ciid, "cart_id": cid, "variant_id": v["variant_id"], "product_name": v["product_name"], "unit_price": v["price"], "quantity": qty, "subtotal": subtotal, "added_at": added_at})
            if "subtotal" in cart_item_columns:
                irows.append((ciid, cid, v["variant_id"], v["product_name"], v["price"], qty, subtotal, added_at))
            else:
                irows.append((ciid, cid, v["variant_id"], v["product_name"], v["price"], qty, added_at))
            ciid += 1
        cid += 1
    execute_many(
        cur,
        f"INSERT INTO carts (cart_id, {cart_user_column}, created_at, updated_at) VALUES (%s, %s, %s, %s)",
        crows,
    )
    if "subtotal" in cart_item_columns:
        execute_many(cur, "INSERT INTO cart_items (cart_item_id, cart_id, variant_id, product_name, unit_price, quantity, subtotal, added_at) VALUES (%s, %s, %s, %s, %s, %s, %s, %s)", irows)
    else:
        execute_many(cur, "INSERT INTO cart_items (cart_item_id, cart_id, variant_id, product_name, unit_price, quantity, added_at) VALUES (%s, %s, %s, %s, %s, %s, %s)", irows)
    return carts, cart_items


def seed_orders(cur, customers, address_map, variants, existing_tables):
    orders, order_items, payments, shipments, shipment_events = [], [], [], [], []
    orows, irows, prows, srows, erows = [], [], [], [], []
    oid = oiid = pid = sid = evid = 1
    order_columns = get_table_columns(cur, "orders")
    order_item_columns = get_table_columns(cur, "order_items")
    shipment_columns = get_table_columns(cur, "shipments") if "shipments" in existing_tables else []
    for idx in range(NUM_ORDERS):
        customer = random.choice(customers)
        created_at = rand_dt(DATA_START_DATE, DATA_END_DATE)
        order_status = weighted_choice(ORDER_STATUS_WEIGHTS)
        payment_method = weighted_choice(PAYMENT_METHODS)
        order_code = f"PS{created_at:%Y%m%d}-{idx + 1:06d}"
        selected = random.sample(variants, k=random.randint(1, min(4, len(variants))))
        subtotal = money(0)
        line_items = []
        for v in selected:
            qty = random.randint(1, 3)
            unit_price = v["price"]
            line_subtotal = money(unit_price * qty)
            subtotal += line_subtotal
            line_items.append((v, qty, unit_price, line_subtotal))

        voucher_code, discount = voucher_discount(subtotal)
        shipping_fee = money(random.choice([0, 15000, 25000, 30000, 40000, 50000]))
        total = money(max(subtotal - discount + shipping_fee, Decimal("0")))
        addr = random.choice(address_map.get(customer["user_id"], [])) if address_map.get(customer["user_id"]) else None
        if addr:
            shipping_name = customer["full_name"]
            shipping_phone = addr["phone"]
            shipping_ward = addr["ward"]
            shipping_district = addr["district"]
            shipping_city = addr["city"]
            shipping_address = f"{addr['street']}, {addr['ward']}, {addr['district']}, {addr['city']}"
        else:
            city, district, ward = random_location()
            shipping_name = customer["full_name"]
            shipping_phone = customer["phone"]
            shipping_ward = ward
            shipping_district = district
            shipping_city = city
            shipping_address = f"{fake.street_address()}, {ward}, {district}, {city}"

        orders.append({"order_id": oid, "order_code": order_code, "status": order_status, "payment_method": payment_method, "total_amount": total, "shipping_fee": shipping_fee, "discount_amount": discount, "applied_voucher": voucher_code, "created_at": created_at, "updated_at": created_at + timedelta(hours=random.randint(1, 72)), "shipping_name": shipping_name, "shipping_phone": shipping_phone, "shipping_address": shipping_address, "shipping_ward": shipping_ward, "shipping_district": shipping_district, "shipping_city": shipping_city, "user_id": customer["user_id"]})
        order_row_map = {
            "order_id": oid,
            "user_id": customer["user_id"],
            "order_code": order_code,
            "status": order_status,
            "total_amount": total,
            "shipping_fee": shipping_fee,
            "discount_amount": discount,
            "applied_voucher": voucher_code,
            "shipping_name": shipping_name,
            "shipping_phone": shipping_phone,
            "shipping_address": shipping_address,
            "shipping_city": shipping_city,
            "created_at": created_at,
            "updated_at": created_at + timedelta(hours=random.randint(1, 72)),
            "payment_method": payment_method,
            "shipping_ward": shipping_ward,
            "shipping_district": shipping_district,
        }
        orders[-1] = order_row_map
        orows.append(tuple(order_row_map[col] for col in order_columns))

        for v, qty, unit_price, line_subtotal in line_items:
            variant_name = f"{v['product_name']} | {v.get('storage_gb') or 'NA'}GB | {v.get('color') or 'DEFAULT'}"
            order_item_row_map = {
                "order_item_id": oiid,
                "order_id": oid,
                "variant_id": v["variant_id"],
                "quantity": qty,
                "unit_price": unit_price,
                "product_name": v["product_name"],
                "variant_name": variant_name,
                "subtotal": line_subtotal,
            }
            order_items.append(order_item_row_map)
            irows.append(tuple(order_item_row_map[col] for col in order_item_columns))
            oiid += 1

        pay_status = payment_status_for(order_status, payment_method)
        paid_at = created_at + timedelta(hours=random.randint(1, 48)) if pay_status == "SUCCESS" else None
        txn_ref = f"TX-{order_code}"
        gateway_no = f"VNP{random.randint(10000000, 99999999)}" if payment_method == "VN_PAY" and pay_status == "SUCCESS" else None
        bank_txn_no = f"BNK{random.randint(10000000, 99999999)}" if pay_status == "SUCCESS" else None
        bank_code = random.choice(["NCB", "VCB", "TCB", "BIDV", "ACB", "MB"]) if payment_method == "VN_PAY" else None
        card_type = random.choice(["ATM", "VISA", "MASTERCARD"]) if payment_method == "VN_PAY" else None
        response_code = "00" if pay_status == "SUCCESS" else ("24" if pay_status == "PENDING" else "99")
        transaction_status = "00" if pay_status == "SUCCESS" else ("01" if pay_status == "FAILED" else "02")
        response_message = {"SUCCESS": "Transaction successful", "PENDING": "Waiting for payment", "FAILED": "Transaction failed", "REFUNDED": "Refunded to customer"}[pay_status]
        pay_date = paid_at.strftime("%Y%m%d%H%M%S") if paid_at else None
        payments.append({"payment_id": pid, "method": payment_method, "status": pay_status, "transaction_ref": txn_ref, "gateway_transaction_no": gateway_no, "bank_transaction_no": bank_txn_no, "bank_code": bank_code, "card_type": card_type, "response_code": response_code, "transaction_status": transaction_status, "pay_date": pay_date, "response_message": response_message, "paid_at": paid_at, "created_at": created_at + timedelta(minutes=random.randint(1, 30)), "updated_at": created_at + timedelta(minutes=random.randint(31, 90)), "order_id": oid})
        prows.append((pid, payment_method, pay_status, txn_ref, gateway_no, bank_txn_no, bank_code, card_type, response_code, transaction_status, pay_date, response_message, paid_at, created_at + timedelta(minutes=random.randint(1, 30)), created_at + timedelta(minutes=random.randint(31, 90)), oid))
        pid += 1

        if "shipments" in existing_tables and order_status in {"CONFIRMED", "PACKING", "SHIPPING", "DELIVERED"} and random.random() < 0.95:
            shipment_status = shipment_status_for(order_status)
            if shipment_status not in {"PENDING", "PICKED_UP", "IN_TRANSIT", "DELIVERED"}:
                shipment_status = "PENDING"
            shipment_row_map = {
                "shipment_id": sid,
                "order_id": oid,
                "carrier": "GHN",
                "tracking_code": f"GHN{oid:08d}",
                "status": shipment_status,
                "estimated_at": created_at + timedelta(days=random.randint(2, 7)),
                "shipped_at": created_at + timedelta(hours=random.randint(2, 24)),
                "delivered_at": created_at + timedelta(hours=random.randint(25, 72)) if shipment_status == "DELIVERED" else None,
            }
            shipments.append(shipment_row_map)
            if shipment_columns:
                srows.append(tuple(shipment_row_map[col] for col in shipment_columns))
            if "shipment_events" in existing_tables:
                for ev in build_shipment_events(order_status, created_at):
                    shipment_events.append({"event_id": evid, "shipment_id": sid, **ev})
                    erows.append((evid, sid, ev["shipment_status"], ev["ghn_status"], ev["event_type"], ev["warehouse"], ev["description"], ev["occurred_at"], ev["raw_payload"]))
                    evid += 1
            sid += 1
        oid += 1

    execute_many(cur, f"INSERT INTO orders ({', '.join(order_columns)}) VALUES ({', '.join(['%s'] * len(order_columns))})", orows)
    execute_many(cur, f"INSERT INTO order_items ({', '.join(order_item_columns)}) VALUES ({', '.join(['%s'] * len(order_item_columns))})", irows)
    execute_many(cur, "INSERT INTO payments (payment_id, method, status, transaction_ref, gateway_transaction_no, bank_transaction_no, bank_code, card_type, response_code, transaction_status, pay_date, response_message, paid_at, created_at, updated_at, order_id) VALUES (%s, %s, %s, %s, %s, %s, %s, %s, %s, %s, %s, %s, %s, %s, %s, %s)", prows)
    if "shipments" in existing_tables:
        if shipment_columns:
            execute_many(cur, f"INSERT INTO shipments ({', '.join(shipment_columns)}) VALUES ({', '.join(['%s'] * len(shipment_columns))})", srows)
    if "shipment_events" in existing_tables:
        execute_many(cur, "INSERT INTO shipment_events (event_id, shipment_id, shipment_status, ghn_status, event_type, warehouse, description, occurred_at, raw_payload) VALUES (%s, %s, %s, %s, %s, %s, %s, %s, %s)", erows)
    return orders, order_items, payments, shipments, shipment_events


def seed_reviews(cur, orders, order_items, variants):
    review_columns = get_table_columns(cur, "reviews")
    variant_map = {v["variant_id"]: v for v in variants}
    order_status_map = {o["order_id"]: o["status"] for o in orders}
    order_user_map = {o["order_id"]: o["user_id"] for o in orders}
    order_date_map = {o["order_id"]: o["created_at"] for o in orders}
    items_by_order = defaultdict(list)
    for item in order_items:
        items_by_order[item["order_id"]].append(item)

    rows, reviews, seen = [], [], set()
    rid = 1
    comments = [
        "Sản phẩm đúng mô tả, dùng ổn định.", "Giao hàng nhanh, đóng gói cẩn thận.",
        "Màn hình đẹp, pin tốt, rất hài lòng.", "Hiệu năng tốt trong tầm giá.",
        "Chất lượng ổn, sẽ tiếp tục ủng hộ.", "Dùng vài ngày thấy khá ổn.",
        "Sản phẩm đẹp, đáng tiền.", "Trải nghiệm tốt, giao hàng đúng hẹn.",
    ]
    for order_id, status in order_status_map.items():
        if status != "DELIVERED" or random.random() > 0.65:
            continue
        customer_id = order_user_map[order_id]
        product_ids = []
        for item in items_by_order[order_id]:
            product_id = variant_map[item["variant_id"]]["product_id"]
            if product_id not in product_ids:
                product_ids.append(product_id)
        for product_id in product_ids:
            if (customer_id, product_id) in seen or random.random() > 0.55:
                continue
            seen.add((customer_id, product_id))
            created_at = order_date_map[order_id] + timedelta(days=random.randint(3, 18))
            updated_at = created_at + timedelta(days=random.randint(0, 10))
            rating = random.choices([1, 2, 3, 4, 5], weights=[2, 4, 12, 32, 50], k=1)[0]
            comment = random.choice(comments)
            review_row = {
                "review_id": rid,
                "user_id": customer_id,
                "product_id": product_id,
                "rating": rating,
                "comment": comment,
                "created_at": created_at,
                "updated_at": updated_at,
            }
            reviews.append(review_row)
            if "updated_at" in review_columns:
                rows.append((rid, customer_id, product_id, rating, comment, created_at, updated_at))
            else:
                rows.append((rid, customer_id, product_id, rating, comment, created_at))
            rid += 1
    if "updated_at" in review_columns:
        execute_many(cur, "INSERT INTO reviews (review_id, user_id, product_id, rating, comment, created_at, updated_at) VALUES (%s, %s, %s, %s, %s, %s, %s)", rows)
    else:
        execute_many(cur, "INSERT INTO reviews (review_id, user_id, product_id, rating, comment, created_at) VALUES (%s, %s, %s, %s, %s, %s)", rows)
    return reviews


def seed_feedback(cur, customers, employees):
    rows, feedbacks = [], []
    fid = 1
    topics = ["Đơn hàng", "Giao hàng", "Sản phẩm", "Bảo hành", "Thanh toán", "Tài khoản"]
    resolutions = ["Đã tư vấn lại cho khách hàng và xử lý xong.", "Đã xác minh thông tin và phản hồi cho khách.", "Đã chuyển bộ phận liên quan xử lý.", "Khách hàng đã được hỗ trợ hoàn tất.", "Đã ghi nhận và đóng ticket."]
    for _ in range(NUM_FEEDBACK):
        customer = random.choice(customers)
        created_at = rand_dt(DATA_START_DATE, DATA_END_DATE)
        status = random.choices(["PENDING", "IN_PROGRESS", "RESOLVED"], weights=[0.42, 0.35, 0.23], k=1)[0]
        employee = random.choice(employees) if status in {"IN_PROGRESS", "RESOLVED"} and random.random() < 0.85 else None
        resolved_at = created_at + timedelta(days=random.randint(1, 7)) if status == "RESOLVED" else None
        resolution = random.choice(resolutions) if status == "RESOLVED" else None
        content = f"{random.choice(topics)}: {fake.sentence(nb_words=random.randint(8, 16))}"
        feedbacks.append({"feedback_id": fid, "content": content, "resolution": resolution, "status": status, "created_at": created_at, "resolved_at": resolved_at, "customer_id": customer["user_id"], "employee_id": employee["user_id"] if employee else None})
        rows.append((fid, content, resolution, status, created_at, resolved_at, customer["user_id"], employee["user_id"] if employee else None))
        fid += 1
    execute_many(cur, "INSERT INTO feedback (feedback_id, content, resolution, status, created_at, resolved_at, customer_id, employee_id) VALUES (%s, %s, %s, %s, %s, %s, %s, %s)", rows)
    return feedbacks


def main():
    conn = connect_db()
    cur = conn.cursor()
    try:
        print(f"Connecting to {DB_CONFIG['database']}...")
        existing_tables = get_existing_tables(cur)
        skipped = truncate_tables(cur, existing_tables)
        conn.commit()
        if skipped:
            print("Skipped missing tables during truncate:", ", ".join(skipped))
        seed_roles(cur)
        conn.commit()
        customers, employees, managers = seed_users(cur)
        conn.commit()
        _, address_map = seed_addresses(cur, customers)
        conn.commit()
        seed_categories(cur)
        conn.commit()
        products, variants, images = seed_products(cur)
        conn.commit()
        carts, cart_items = seed_carts(cur, customers, variants)
        conn.commit()
        orders, order_items, payments, shipments, shipment_events = seed_orders(cur, customers, address_map, variants, existing_tables)
        conn.commit()
        reviews = seed_reviews(cur, orders, order_items, variants)
        conn.commit()
        feedbacks = seed_feedback(cur, customers, employees)
        conn.commit()

        print("Done.")
        print(f"roles: {len(ROLE_ROWS)}")
        print(f"users: managers={len(managers)}, employees={len(employees)}, customers={len(customers)}")
        print(f"addresses: {sum(len(v) for v in address_map.values())}")
        print(f"categories: {len(CATEGORY_ROWS)}")
        print(f"products: {len(products)}")
        print(f"variants: {len(variants)}")
        print(f"images: {len(images)}")
        print(f"carts: {len(carts)}")
        print(f"cart items: {len(cart_items)}")
        print(f"orders: {len(orders)}")
        print(f"order items: {len(order_items)}")
        print(f"payments: {len(payments)}")
        print(f"shipments: {len(shipments)}")
        print(f"shipment events: {len(shipment_events)}")
        print(f"reviews: {len(reviews)}")
        print(f"feedback: {len(feedbacks)}")
        print("Example logins:")
        print("  manager1@gmail.com / 123456")
        print("  employee1@gmail.com / 123456")
        print("  customer0001@phoneshop.vn / 123456")
    except Exception:
        conn.rollback()
        raise
    finally:
        cur.close()
        conn.close()


if __name__ == "__main__":
    main()
