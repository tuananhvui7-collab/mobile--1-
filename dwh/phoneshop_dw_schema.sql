CREATE DATABASE IF NOT EXISTS phoneshop_dw
    CHARACTER SET utf8mb4
    COLLATE utf8mb4_unicode_ci;

USE phoneshop_dw;

DROP TABLE IF EXISTS Fact_Sales;
DROP TABLE IF EXISTS Fact_Reviews;
DROP TABLE IF EXISTS Fact_OPEX;
DROP TABLE IF EXISTS Dim_Customer;
DROP TABLE IF EXISTS Dim_Product;
DROP TABLE IF EXISTS Dim_Date;

CREATE TABLE Dim_Date (
    date_key        INT         NOT NULL,
    full_date       DATE        NOT NULL,
    day             TINYINT     NOT NULL,
    month           TINYINT     NOT NULL,
    month_name      VARCHAR(20) NOT NULL,
    quarter         TINYINT     NOT NULL,
    year            SMALLINT    NOT NULL,
    day_of_week     TINYINT     NOT NULL,
    day_name        VARCHAR(20) NOT NULL,
    is_weekend      BOOLEAN     NOT NULL,
    month_key       INT         NOT NULL,
    PRIMARY KEY (date_key)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE Dim_Product (
    product_key     BIGINT       NOT NULL,
    product_name    VARCHAR(255) NOT NULL,
    variant_name    VARCHAR(255) NOT NULL,
    category_name   VARCHAR(100) NOT NULL,
    parent_category VARCHAR(100),
    color           VARCHAR(50),
    storage         VARCHAR(20),
    current_price   DECIMAL(19,2) NOT NULL,
    import_price    DECIMAL(19,2) NOT NULL,
    current_stock   INT          NOT NULL,
    sku             VARCHAR(255),
    status          VARCHAR(20),
    PRIMARY KEY (product_key)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE Dim_Customer (
    customer_key    BIGINT       NOT NULL,
    full_name       VARCHAR(255) NOT NULL,
    email           VARCHAR(255) NOT NULL,
    role_name       VARCHAR(50)  NOT NULL,
    city            VARCHAR(100),
    registered_at   DATETIME,
    PRIMARY KEY (customer_key)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE Fact_Sales (
    sales_key           BIGINT AUTO_INCREMENT PRIMARY KEY,
    date_key            INT           NOT NULL,
    customer_key        BIGINT        NOT NULL,
    product_key         BIGINT        NOT NULL,
    order_id            BIGINT        NOT NULL,
    order_status        VARCHAR(20)   NOT NULL,
    payment_method      VARCHAR(20),
    is_voucher_applied  BOOLEAN       NOT NULL DEFAULT FALSE,
    lead_time_days      DECIMAL(5,1),
    quantity            INT           NOT NULL,
    unit_price          DECIMAL(19,2) NOT NULL,
    revenue             DECIMAL(19,2) NOT NULL,
    cogs                DECIMAL(19,2) NOT NULL,
    gross_profit        DECIMAL(19,2) NOT NULL,
    discount_amount     DECIMAL(19,2) NOT NULL DEFAULT 0,
    shipping_fee        DECIMAL(19,2) NOT NULL DEFAULT 0,
    INDEX idx_date      (date_key),
    INDEX idx_customer  (customer_key),
    INDEX idx_product   (product_key),
    INDEX idx_order     (order_id),
    INDEX idx_status    (order_status)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE Fact_Reviews (
    review_key      BIGINT AUTO_INCREMENT PRIMARY KEY,
    date_key        INT         NOT NULL,
    customer_key    BIGINT      NOT NULL,
    product_key     BIGINT      NOT NULL,
    order_id        BIGINT      NOT NULL,
    rating          TINYINT     NOT NULL,
    INDEX idx_date      (date_key),
    INDEX idx_product   (product_key),
    INDEX idx_customer  (customer_key)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE Fact_OPEX (
    opex_key        INT AUTO_INCREMENT PRIMARY KEY,
    month_key       INT           NOT NULL,
    year            SMALLINT      NOT NULL,
    month           TINYINT       NOT NULL,
    total_salaries  DECIMAL(19,2) NOT NULL,
    other_opex      DECIMAL(19,2) NOT NULL DEFAULT 0,
    total_opex      DECIMAL(19,2) NOT NULL,
    UNIQUE KEY uq_month (month_key)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;
