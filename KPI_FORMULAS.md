# KPI và Công Thức Báo Cáo PhoneShop

Tài liệu này mô tả KPI theo 2 lớp:

1. **Công thức toán học / nghiệp vụ** để trình bày trong báo cáo.
2. **Công thức DAX** để hiện thực trong Power BI nếu cần.

Các công thức bên dưới được viết theo đúng cấu trúc dữ liệu hiện tại của kho phân tích `phoneshop_dw`.

---

## 1. Nguồn dữ liệu dùng cho KPI

### Bảng fact

- `Fact_Sales`
  - dùng cho doanh thu, đơn hàng, chi phí hàng bán, lợi nhuận, chiết khấu, phí ship, lead time
- `Fact_Reviews`
  - dùng cho đánh giá sản phẩm và CSAT
- `Fact_OPEX`
  - dùng cho chi phí vận hành, lương, lợi nhuận vận hành

### Bảng dimension

- `Dim_Date`
  - dùng để lọc theo ngày / tháng / quý / năm
- `Dim_Product`
  - dùng để lọc theo sản phẩm, biến thể, danh mục, tồn kho
- `Dim_Customer`
  - dùng để lọc theo khách hàng, thành phố, vai trò

### Lưu ý quan trọng

`Fact_Sales` có grain là **mỗi dòng chi tiết đơn hàng**.
Vì vậy các chỉ số cấp đơn như `discount_amount` và `shipping_fee` phải được cộng theo `order_id` để tránh đếm lặp.

---

## 1.1. Ký hiệu công thức

Để mô tả KPI theo kiểu toán học, mình dùng các ký hiệu sau:

- `S` = tập tất cả dòng trong `Fact_Sales`
- `R` = tập tất cả dòng trong `Fact_Reviews`
- `O` = tập tất cả dòng trong `Fact_OPEX`
- `o` = một đơn hàng
- `i` = một dòng chi tiết đơn hàng
- `p` = một review
- `v` = một biến thể sản phẩm

Hàm tổng hợp:

- `SUM(x)` = tổng
- `COUNT(x)` = đếm số dòng
- `DISTINCTCOUNT(x)` = đếm giá trị khác nhau
- `AVG(x)` = trung bình
- `MAX(x)` = giá trị lớn nhất
- `DIVIDE(a, b)` = `a / b` nếu `b != 0`, ngược lại trả về 0

---

## 2. Nhóm KPI doanh thu và đơn hàng

### 2.1. Tổng doanh thu gộp

**Ý nghĩa:** Tổng doanh thu trước khi trừ giảm giá.

**Công thức toán học:**

`Total Revenue = Σ revenue(i), với i ∈ S`

```DAX
Total Revenue = SUM(Fact_Sales[revenue])
```

### 2.2. Tổng chiết khấu

**Ý nghĩa:** Tổng số tiền giảm giá áp dụng cho các đơn hàng.

**Công thức toán học:**

`Total Discounts = Σ discount(o), với o là mỗi order_id khác nhau`

```DAX
Total Discounts =
SUMX(
    VALUES(Fact_Sales[order_id]),
    CALCULATE(MAX(Fact_Sales[discount_amount]))
)
```

### 2.3. Doanh thu thuần

**Ý nghĩa:** Doanh thu gộp trừ chiết khấu.

**Công thức toán học:**

`Net Revenue = Total Revenue - Total Discounts`

```DAX
Net Revenue = [Total Revenue] - [Total Discounts]
```

### 2.4. Tổng giá vốn hàng bán

**Ý nghĩa:** Tổng COGS của toàn bộ đơn hàng.

**Công thức toán học:**

`Total COGS = Σ cogs(i), với i ∈ S`

```DAX
Total COGS = SUM(Fact_Sales[cogs])
```

### 2.5. Lợi nhuận gộp

**Ý nghĩa:** Doanh thu thuần trừ giá vốn.

**Công thức toán học:**

`Gross Profit = Net Revenue - Total COGS`

```DAX
Gross Profit = [Net Revenue] - [Total COGS]
```

### 2.6. Biên lợi nhuận gộp

**Ý nghĩa:** Tỷ lệ lợi nhuận gộp trên doanh thu thuần.

**Công thức toán học:**

`Gross Margin % = Gross Profit / Net Revenue`

```DAX
Gross Margin % =
DIVIDE([Gross Profit], [Net Revenue], 0)
```

### 2.7. Tổng số đơn hàng

**Ý nghĩa:** Tổng số đơn phát sinh trong kho dữ liệu.

**Công thức toán học:**

`Total Orders = count(order_id)`

```DAX
Total Orders = DISTINCTCOUNT(Fact_Sales[order_id])
```

### 2.8. Số đơn đã giao

**Ý nghĩa:** Số đơn ở trạng thái hoàn tất giao hàng.

**Công thức toán học:**

`Delivered Orders = count(order_id where order_status = DELIVERED)`

```DAX
Delivered Orders =
CALCULATE(
    DISTINCTCOUNT(Fact_Sales[order_id]),
    Fact_Sales[order_status] = "DELIVERED"
)
```

### 2.9. Số đơn đã hủy

**Ý nghĩa:** Số đơn bị hủy trong kỳ.

**Công thức toán học:**

`Cancelled Orders = count(order_id where order_status = CANCELLED)`

```DAX
Cancelled Orders =
CALCULATE(
    DISTINCTCOUNT(Fact_Sales[order_id]),
    Fact_Sales[order_status] = "CANCELLED"
)
```

### 2.10. Tỷ lệ hủy đơn

**Ý nghĩa:** Tỷ lệ đơn hủy trên tổng đơn.

**Công thức toán học:**

`Cancel Rate = Cancelled Orders / Total Orders`

```DAX
Cancel Rate =
DIVIDE([Cancelled Orders], [Total Orders], 0)
```

### 2.11. Tổng số sản phẩm bán ra

**Ý nghĩa:** Tổng số lượng sản phẩm đã bán.

**Công thức toán học:**

`Total Units Sold = Σ quantity(i), với i ∈ S`

```DAX
Total Units Sold = SUM(Fact_Sales[quantity])
```

### 2.12. Giá trị đơn hàng trung bình

**Ý nghĩa:** Trung bình doanh thu thuần / đơn.

**Công thức toán học:**

`AOV = Net Revenue / Total Orders`

```DAX
AOV = DIVIDE([Net Revenue], [Total Orders], 0)
```

### 2.13. Số đơn có áp dụng voucher

**Ý nghĩa:** Đếm số đơn có mã giảm giá.

**Công thức toán học:**

`Orders With Voucher = count(order_id where is_voucher_applied = true)`

```DAX
Orders With Voucher =
CALCULATE(
    DISTINCTCOUNT(Fact_Sales[order_id]),
    Fact_Sales[is_voucher_applied] = TRUE()
)
```

### 2.14. Tỷ lệ dùng voucher

**Ý nghĩa:** Tỷ lệ đơn có voucher trên tổng đơn.

**Công thức toán học:**

`Voucher Penetration = Orders With Voucher / Total Orders`

```DAX
Voucher Penetration =
DIVIDE([Orders With Voucher], [Total Orders], 0)
```

### 2.15. Lead time trung bình

**Ý nghĩa:** Thời gian giao hàng trung bình theo đơn.

**Công thức toán học:**

`Average Lead Time = AVG(lead_time_days)`

```DAX
Average Lead Time (Days) =
AVERAGEX(
    VALUES(Fact_Sales[order_id]),
    CALCULATE(MAX(Fact_Sales[lead_time_days]))
)
```

---

## 3. Nhóm KPI đánh giá và CSAT

### 3.1. Số lượt đánh giá

**Ý nghĩa:** Tổng số review khách hàng để lại.

**Công thức toán học:**

`Review Count = count(review_id)`

```DAX
Review Count = COUNTROWS(Fact_Reviews)
```

### 3.2. Điểm đánh giá trung bình

**Ý nghĩa:** Mức hài lòng trung bình của khách hàng.

**Công thức toán học:**

`Average Rating = AVG(rating)`

```DAX
Average Rating = AVERAGE(Fact_Reviews[rating])
```

---

## 4. Nhóm KPI chi phí và lợi nhuận vận hành

### 4.1. Tổng lương

**Ý nghĩa:** Tổng chi phí lương nhân sự trong kỳ.

**Công thức toán học:**

`Total Salaries = Σ total_salaries(m), với m ∈ O`

```DAX
Total Salaries = SUM(Fact_OPEX[total_salaries])
```

### 4.2. Tổng chi phí vận hành

**Ý nghĩa:** Tổng OPEX trong kỳ.

**Công thức toán học:**

`Total OPEX = Σ total_opex(m), với m ∈ O`

```DAX
Total OPEX = SUM(Fact_OPEX[total_opex])
```

### 4.3. Lợi nhuận vận hành

**Ý nghĩa:** Lợi nhuận gộp trừ chi phí vận hành.

**Công thức toán học:**

`Operating Profit = Gross Profit - Total OPEX`

```DAX
Operating Profit = [Gross Profit] - [Total OPEX]
```

### 4.4. Tỷ lệ chi phí lương

**Ý nghĩa:** Tỷ trọng tiền lương trên doanh thu thuần.

**Công thức toán học:**

`Labor Cost Ratio = Total Salaries / Net Revenue`

```DAX
Labor Cost Ratio = DIVIDE([Total Salaries], [Net Revenue], 0)
```

---

## 5. Nhóm KPI tồn kho và rủi ro

### 5.1. Số biến thể tồn thấp

**Ý nghĩa:** Số biến thể có tồn kho dưới ngưỡng cảnh báo.

**Công thức toán học:**

`Low Stock Variants = count(v ∈ Dim_Product where current_stock(v) <= 5)`

```DAX
Low Stock Variants =
CALCULATE(
    COUNTROWS(Dim_Product),
    Dim_Product[current_stock] <= 5
)
```

### 5.2. Cờ cảnh báo tồn kho thấp

**Ý nghĩa:** Dùng để hiển thị cảnh báo tổng quan trên dashboard.

**Công thức toán học:**

`Low Stock Alert = 1 if Low Stock Variants > 0 else 0`

```DAX
Low Stock Alert =
IF([Low Stock Variants] > 0, 1, 0)
```

---

## 6. Thứ tự tạo measure trong Power BI

Nên tạo theo thứ tự:

1. `Total Revenue`
2. `Total Discounts`
3. `Net Revenue`
4. `Total COGS`
5. `Gross Profit`
6. `Gross Margin %`
7. `Total Orders`
8. `Delivered Orders`
9. `Cancelled Orders`
10. `Cancel Rate`
11. `Total Units Sold`
12. `AOV`
13. `Orders With Voucher`
14. `Voucher Penetration`
15. `Average Lead Time (Days)`
16. `Review Count`
17. `Average Rating`
18. `Total Salaries`
19. `Total OPEX`
20. `Operating Profit`
21. `Labor Cost Ratio`
22. `Low Stock Variants`
23. `Low Stock Alert`

---

## 7. Gợi ý dùng trong báo cáo

- Dùng `Net Revenue`, `Gross Profit`, `Gross Margin %`, `Operating Profit` cho dashboard điều hành
- Dùng `AOV`, `Cancel Rate`, `Voucher Penetration`, `Average Lead Time (Days)` cho vận hành
- Dùng `Review Count`, `Average Rating` cho CSAT
- Dùng `Low Stock Variants`, `Low Stock Alert` cho cảnh báo tồn kho

## 8. Thực tế làm ETL và số lượng file

Trong dự án thực tế, không ai bắt buộc phải có rất nhiều file ETL.
Thường có 3 cách làm:

### Cách 1. Một file ETL chính, bên trong chia hàm

- 1 entrypoint
- chia hàm theo từng bước:
  - extract
  - transform
  - load dimension
  - load fact
  - validate

Đây là cách gọn và rất hợp cho đồ án / project nhỏ đến vừa.

### Cách 2. Nhiều file ETL theo domain

- `etl_customer.py`
- `etl_product.py`
- `etl_sales.py`
- `etl_review.py`
- `etl_opex.py`

Cách này dùng khi dữ liệu lớn hơn, hoặc có nhiều nguồn khác nhau.

### Cách 3. Dùng orchestrator

- Airflow / Prefect / dbt / cron job
- mỗi job làm một phần nhỏ
- có log, retry, schedule, monitoring

### Với dự án PhoneShop nên làm gì?

Với scope hiện tại, **1 file ETL chính là đủ** nếu:

- code đã chia hàm rõ ràng
- có thể nạp lại toàn bộ DWH
- dễ bảo trì
- dễ giải thích trong báo cáo

Nếu muốn chuyên nghiệp hơn một chút, nên tách logic bên trong thành các module nhỏ, nhưng vẫn giữ **1 script chạy chính**.

### Kết luận thực tế

Sau khi chuyển sang Metabase, bạn **không cần thêm nhiều file ETL chỉ vì đổi dashboard tool**.
Metabase chỉ thay lớp hiển thị.
ETL chỉ cần phục vụ dữ liệu cho DWH ổn định là đủ.

Nếu sau này có thêm nguồn dữ liệu mới, lúc đó mới nên tách ETL thành nhiều job hơn.
