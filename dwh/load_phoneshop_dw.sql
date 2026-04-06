USE phoneshop_dw;

SET SESSION cte_max_recursion_depth = 5000;
SET FOREIGN_KEY_CHECKS = 0;
TRUNCATE TABLE Fact_Sales;
TRUNCATE TABLE Fact_Reviews;
TRUNCATE TABLE Fact_OPEX;
TRUNCATE TABLE Dim_Customer;
TRUNCATE TABLE Dim_Product;
TRUNCATE TABLE Dim_Date;
SET FOREIGN_KEY_CHECKS = 1;

INSERT INTO Dim_Date
    (date_key, full_date, day, month, month_name, quarter, year, day_of_week, day_name, is_weekend, month_key)
WITH RECURSIVE bounds AS (
    SELECT
        COALESCE(MIN(src_date), DATE('2023-01-01')) AS start_date,
        COALESCE(MAX(src_date), DATE('2026-03-31')) AS end_date
    FROM (
        SELECT DATE(created_at) AS src_date FROM phoneshop_db.orders
        UNION ALL
        SELECT DATE(created_at) AS src_date FROM phoneshop_db.reviews
    ) dates_source
),
dates AS (
    SELECT start_date AS d, end_date
    FROM bounds
    UNION ALL
    SELECT DATE_ADD(d, INTERVAL 1 DAY), end_date
    FROM dates
    WHERE d < end_date
)
SELECT
    YEAR(d) * 10000 + MONTH(d) * 100 + DAY(d) AS date_key,
    d AS full_date,
    DAY(d) AS day,
    MONTH(d) AS month,
    CASE MONTH(d)
        WHEN 1 THEN 'Thang 1'
        WHEN 2 THEN 'Thang 2'
        WHEN 3 THEN 'Thang 3'
        WHEN 4 THEN 'Thang 4'
        WHEN 5 THEN 'Thang 5'
        WHEN 6 THEN 'Thang 6'
        WHEN 7 THEN 'Thang 7'
        WHEN 8 THEN 'Thang 8'
        WHEN 9 THEN 'Thang 9'
        WHEN 10 THEN 'Thang 10'
        WHEN 11 THEN 'Thang 11'
        WHEN 12 THEN 'Thang 12'
    END AS month_name,
    QUARTER(d) AS quarter,
    YEAR(d) AS year,
    WEEKDAY(d) + 1 AS day_of_week,
    CASE WEEKDAY(d)
        WHEN 0 THEN 'Thu 2'
        WHEN 1 THEN 'Thu 3'
        WHEN 2 THEN 'Thu 4'
        WHEN 3 THEN 'Thu 5'
        WHEN 4 THEN 'Thu 6'
        WHEN 5 THEN 'Thu 7'
        WHEN 6 THEN 'Chu nhat'
    END AS day_name,
    CASE WHEN WEEKDAY(d) >= 5 THEN 1 ELSE 0 END AS is_weekend,
    YEAR(d) * 100 + MONTH(d) AS month_key
FROM dates;

INSERT INTO Dim_Product
    (product_key, product_name, variant_name, category_name, parent_category, color, storage, current_price, import_price, current_stock, sku, status)
SELECT
    v.variant_id AS product_key,
    p.name AS product_name,
    CONCAT_WS(' | ',
        p.name,
        CASE
            WHEN v.storage_gb IS NULL THEN NULL
            ELSE CONCAT(v.storage_gb, 'GB')
        END,
        NULLIF(v.color, '')
    ) AS variant_name,
    c.name AS category_name,
    pc.name AS parent_category,
    v.color,
    CASE WHEN v.storage_gb IS NULL THEN NULL ELSE CONCAT(v.storage_gb) END AS storage,
    v.price AS current_price,
    v.import_price,
    v.stock_qty AS current_stock,
    v.sku,
    v.status
FROM phoneshop_db.product_variants v
JOIN phoneshop_db.products p ON p.product_id = v.product_id
LEFT JOIN phoneshop_db.categories c ON c.category_id = p.category_id
LEFT JOIN phoneshop_db.categories pc ON pc.category_id = c.parent_id;

INSERT INTO Dim_Customer
    (customer_key, full_name, email, role_name, city, registered_at)
SELECT
    u.user_id,
    u.full_name,
    u.email,
    COALESCE(r.name_role, u.dtype) AS role_name,
    a.city,
    u.created_at
FROM phoneshop_db.users u
LEFT JOIN phoneshop_db.roles r ON r.role_id = u.role_id
LEFT JOIN (
    SELECT user_id, city
    FROM phoneshop_db.address
    WHERE is_default = 1
) a ON a.user_id = u.user_id
WHERE u.dtype = 'CUSTOMER';

INSERT INTO Fact_Sales
    (date_key, customer_key, product_key, order_id, order_status, payment_method, is_voucher_applied, lead_time_days, quantity, unit_price, revenue, cogs, gross_profit, discount_amount, shipping_fee)
SELECT
    YEAR(o.created_at) * 10000 + MONTH(o.created_at) * 100 + DAY(o.created_at) AS date_key,
    o.user_id AS customer_key,
    v.variant_id AS product_key,
    o.order_id,
    o.status AS order_status,
    o.payment_method,
    CASE WHEN o.applied_voucher IS NULL OR o.applied_voucher = '' THEN 0 ELSE 1 END AS is_voucher_applied,
    CASE
        WHEN s.delivered_at IS NOT NULL THEN TIMESTAMPDIFF(MINUTE, o.created_at, s.delivered_at) / 1440.0
        WHEN s.shipped_at IS NOT NULL THEN TIMESTAMPDIFF(MINUTE, o.created_at, s.shipped_at) / 1440.0
        ELSE NULL
    END AS lead_time_days,
    oi.quantity,
    oi.unit_price,
    oi.quantity * oi.unit_price AS revenue,
    oi.quantity * COALESCE(v.import_price, 0) AS cogs,
    (oi.quantity * oi.unit_price) - (oi.quantity * COALESCE(v.import_price, 0)) AS gross_profit,
    o.discount_amount,
    o.shipping_fee
FROM phoneshop_db.order_items oi
JOIN phoneshop_db.orders o ON o.order_id = oi.order_id
JOIN phoneshop_db.product_variants v ON v.variant_id = oi.variant_id
LEFT JOIN phoneshop_db.shipments s ON s.order_id = o.order_id;

INSERT INTO Fact_Reviews
    (date_key, customer_key, product_key, order_id, rating)
SELECT
    YEAR(r.created_at) * 10000 + MONTH(r.created_at) * 100 + DAY(r.created_at) AS date_key,
    r.user_id AS customer_key,
    r.product_id AS product_key,
    (
        SELECT o.order_id
        FROM phoneshop_db.orders o
        JOIN phoneshop_db.order_items oi ON oi.order_id = o.order_id
        JOIN phoneshop_db.product_variants v ON v.variant_id = oi.variant_id
        WHERE o.user_id = r.user_id
          AND v.product_id = r.product_id
          AND o.status = 'DELIVERED'
          AND o.created_at <= r.created_at
        ORDER BY o.created_at DESC, o.order_id DESC
        LIMIT 1
    ) AS order_id,
    r.rating
FROM phoneshop_db.reviews r
WHERE (
    SELECT o.order_id
    FROM phoneshop_db.orders o
    JOIN phoneshop_db.order_items oi ON oi.order_id = o.order_id
    JOIN phoneshop_db.product_variants v ON v.variant_id = oi.variant_id
    WHERE o.user_id = r.user_id
      AND v.product_id = r.product_id
      AND o.status = 'DELIVERED'
      AND o.created_at <= r.created_at
    ORDER BY o.created_at DESC, o.order_id DESC
    LIMIT 1
) IS NOT NULL;

INSERT INTO Fact_OPEX
    (month_key, year, month, total_salaries, other_opex, total_opex)
WITH RECURSIVE bounds AS (
    SELECT
        COALESCE(MIN(month_start), DATE('2023-01-01')) AS start_month,
        COALESCE(MAX(month_start), DATE_FORMAT(CURDATE(), '%Y-%m-01')) AS end_month
    FROM (
        SELECT DATE_FORMAT(COALESCE(hire_date, created_at), '%Y-%m-01') AS month_start
        FROM phoneshop_db.users
        WHERE dtype IN ('EMPLOYEE', 'MANAGER')
        UNION ALL
        SELECT DATE_FORMAT(created_at, '%Y-%m-01') AS month_start
        FROM phoneshop_db.orders
    ) months_source
),
months AS (
    SELECT start_month AS m, end_month
    FROM bounds
    UNION ALL
    SELECT DATE_ADD(m, INTERVAL 1 MONTH), end_month
    FROM months
    WHERE m < end_month
)
SELECT
    YEAR(m) * 100 + MONTH(m) AS month_key,
    YEAR(m) AS year,
    MONTH(m) AS month,
    COALESCE(SUM(CASE
        WHEN DATE_FORMAT(COALESCE(u.hire_date, u.created_at), '%Y-%m-01') <= m THEN COALESCE(u.salary, 0)
        ELSE 0
    END), 0) AS total_salaries,
    0 AS other_opex,
    COALESCE(SUM(CASE
        WHEN DATE_FORMAT(COALESCE(u.hire_date, u.created_at), '%Y-%m-01') <= m THEN COALESCE(u.salary, 0)
        ELSE 0
    END), 0) AS total_opex
FROM months
LEFT JOIN phoneshop_db.users u
    ON u.dtype IN ('EMPLOYEE', 'MANAGER')
GROUP BY m;
