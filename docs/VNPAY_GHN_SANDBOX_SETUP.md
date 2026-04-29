# Hướng Dẫn Setup Sandbox VNPay Và GHN

Tài liệu này dùng để chuyển project từ mock local sang sandbox thật cho:

- VNPay
- GHN

File này được đặt ở `docs/` ngoài `mobile/` để bạn dùng như runbook vận hành.

---

## 1. Mục tiêu

Sau khi làm xong tài liệu này:

- app vẫn chạy local với MySQL như hiện tại
- có thể bật `VNPay sandbox` thật
- có thể bật `GHN sandbox` thật
- không cần hardcode secret vào `application.properties`

---

## 2. Điều kiện trước khi làm

Bạn cần có:

- MySQL local chạy được
- app Spring Boot đang chạy được local
- tài khoản sandbox VNPay
- tài khoản GHN dev / sandbox
- một URL public để nhận callback/webhook nếu muốn test end-to-end thật

URL public có thể lấy bằng:

- `ngrok`
- `cloudflared tunnel`
- hoặc deploy tạm app lên một máy/public host

Nếu không có URL public:

- vẫn test được một phần luồng tạo payment request / tạo shipment
- nhưng callback từ VNPay và webhook từ GHN sẽ không hoàn chỉnh

---

## 3. Biến môi trường app đang hỗ trợ

App hiện đọc config từ environment variable trước, rồi mới fallback về default local.

### Database

```powershell
$env:APP_DB_URL="jdbc:mysql://localhost:3306/phoneshop_db?useUnicode=true&characterEncoding=UTF-8&serverTimezone=Asia/Ho_Chi_Minh"
$env:APP_DB_USERNAME="root"
$env:APP_DB_PASSWORD="root"
```

### Mail

```powershell
$env:APP_MAIL_HOST="smtp.gmail.com"
$env:APP_MAIL_PORT="587"
$env:APP_MAIL_USERNAME="your_mail@gmail.com"
$env:APP_MAIL_PASSWORD="your_app_password"
```

### VNPay

```powershell
$env:VNPAY_PAYMENT_URL="https://sandbox.vnpayment.vn/paymentv2/vpcpay.html"
$env:VNPAY_TMN_CODE="YOUR_REAL_SANDBOX_TMN_CODE"
$env:VNPAY_HASH_SECRET="YOUR_REAL_SANDBOX_HASH_SECRET"
$env:VNPAY_RETURN_URL="https://your-public-domain/payments/vnpay/return"
$env:VNPAY_IPN_URL="https://your-public-domain/payments/vnpay/ipn"
$env:VNPAY_MOCK_MODE="false"
```

### GHN

```powershell
$env:GHN_API_BASE_URL="https://dev-online-gateway.ghn.vn/shiip/public-api/v2"
$env:GHN_TOKEN="YOUR_REAL_GHN_TOKEN"
$env:GHN_SHOP_ID="YOUR_REAL_GHN_SHOP_ID"
$env:GHN_FROM_NAME="PhoneShop"
$env:GHN_FROM_PHONE="0900000000"
$env:GHN_FROM_ADDRESS="1 Nguyen Trai, P. Ben Thanh"
$env:GHN_FROM_WARD_NAME="Ben Thanh"
$env:GHN_FROM_DISTRICT_NAME="Quan 1"
$env:GHN_FROM_PROVINCE_NAME="Ho Chi Minh"
$env:GHN_RETURN_PHONE="0900000000"
$env:GHN_RETURN_ADDRESS="1 Nguyen Trai, P. Ben Thanh"
$env:GHN_RETURN_WARD_NAME="Ben Thanh"
$env:GHN_RETURN_DISTRICT_NAME="Quan 1"
$env:GHN_RETURN_PROVINCE_NAME="Ho Chi Minh"
```

---

## 4. Setup VNPay Sandbox Thật

### Bước 1. Lấy credential

Bạn cần lấy từ VNPay sandbox:

- `TMN Code`
- `Hash Secret`

Nếu chưa có tài khoản sandbox:

1. đăng ký với VNPay sandbox
2. xin merchant test
3. lấy thông tin cấu hình test

### Bước 2. Khai báo URL callback

Bạn cần cấu hình:

- `returnUrl`
- `ipnUrl`

Hai URL này phải public nếu muốn VNPay gọi về thật.

Ví dụ:

```text
https://abc123.ngrok-free.app/payments/vnpay/return
https://abc123.ngrok-free.app/payments/vnpay/ipn
```

### Bước 3. Tắt mock mode

```powershell
$env:VNPAY_MOCK_MODE="false"
```

### Bước 4. Chạy app

```powershell
cd mobile
.\mvnw.cmd spring-boot:run
```

### Bước 5. Test luồng

1. login customer
2. add item vào cart
3. checkout
4. chọn `VN_PAY`
5. place order
6. app phải redirect sang domain sandbox VNPay
7. thanh toán test xong, VNPay redirect về `/payments/vnpay/return`
8. nếu IPN hoạt động đúng, payment status phải cập nhật trong DB

### Bước 6. Kiểm tra kết quả

Kiểm tra:

- bảng `payments`
- trang chi tiết payment
- trang chi tiết order

Trạng thái mong đợi:

- `response_code`
- `transaction_status`
- `paid_at`
- `status = SUCCESS` hoặc `FAILED`

### Lưu ý

- Nếu `VNPAY_MOCK_MODE=true`, app sẽ không ra cổng VNPay thật.
- Nếu `returnUrl` đúng nhưng `ipnUrl` không public, callback trình duyệt có thể chạy nhưng IPN server-to-server có thể fail.

---

## 5. Setup GHN Sandbox Thật

### Bước 1. Lấy credential GHN

Bạn cần:

- `Token`
- `Shop ID`

Lấy từ GHN dev portal hoặc tài khoản sandbox mà bạn đang có.

### Bước 2. Xác nhận địa chỉ shop gửi hàng

Các trường này phải đúng với dữ liệu GHN chấp nhận:

- `GHN_FROM_NAME`
- `GHN_FROM_PHONE`
- `GHN_FROM_ADDRESS`
- `GHN_FROM_WARD_NAME`
- `GHN_FROM_DISTRICT_NAME`
- `GHN_FROM_PROVINCE_NAME`

Nếu dữ liệu địa chỉ không map đúng địa bàn GHN:

- tạo đơn GHN có thể fail
- refresh tracking có thể không ra kết quả hợp lệ

### Bước 3. Chạy app với credential thật

```powershell
$env:GHN_TOKEN="..."
$env:GHN_SHOP_ID="..."
cd mobile
.\mvnw.cmd spring-boot:run
```

### Bước 4. Test tạo shipment

Flow nên test:

1. customer đặt COD order
2. employee chuyển đơn qua `CONFIRMED -> PACKING -> SHIPPING`
3. khi sang `SHIPPING`, app sẽ cố tạo order trên GHN
4. DB bảng `shipments` phải có:
   - `client_order_code`
   - `ghn_order_code`
   - `tracking_url`

### Bước 5. Test refresh tracking

Mở:

- tracking page của customer
- hoặc action refresh trong shipment flow nếu bạn đã có nút

Mong đợi:

- `ghn_status` được cập nhật
- `shipment_events` có thêm event
- `status_message` thay đổi

### Bước 6. Test webhook GHN

Bạn cần URL public cho webhook, ví dụ:

```text
https://abc123.ngrok-free.app/webhooks/ghn
```

Sau đó cấu hình webhook phía GHN nếu portal hỗ trợ.

Mong đợi: AJAX 

- GHN gửi trạng thái mới về app
- app cập nhật `shipments`
- app thêm `shipment_events`

### Lưu ý

- Nếu chưa có webhook thật, vẫn có thể test bằng refresh polling/manual sync.
- Nếu sandbox GHN không tạo đơn được, kiểm tra đầu tiên là token/shop/address.

---

## 6. Cách chạy local với tunnel public

Ví dụ dùng `ngrok`:

### Chạy app local

```powershell
cd mobile
.\mvnw.cmd spring-boot:run
```

### Mở tunnel

```powershell
ngrok http 8080
```

### Lấy URL public

Ví dụ:

```text
https://abc123.ngrok-free.app
```

Rồi set:

```powershell
$env:VNPAY_RETURN_URL="https://abc123.ngrok-free.app/payments/vnpay/return"
$env:VNPAY_IPN_URL="https://abc123.ngrok-free.app/payments/vnpay/ipn"
```

Webhook GHN:

```text
https://abc123.ngrok-free.app/webhooks/ghn
```

Sau khi đổi env var, restart app.

---

## 7. Checklist test thật

### VNPay

- app redirect ra sandbox VNPay
- return URL quay về app thành công
- payment record được update
- order detail phản ánh trạng thái payment đúng

### GHN

- khi order sang `SHIPPING`, shipment được tạo
- có `ghn_order_code`
- refresh tracking lấy được trạng thái
- webhook cập nhật được event nếu cấu hình public URL

---

## 8. Lỗi thường gặp

### VNPay không redirect ra ngoài

Kiểm tra:

- `VNPAY_MOCK_MODE=false`
- `TMN_CODE` và `HASH_SECRET` đã set
- flow đang đi vào payment thật chứ không còn mock

### VNPay callback về nhưng payment không update

Kiểm tra:

- `returnUrl`
- `ipnUrl`
- log chữ ký/hash
- mapping response code trong service payment

### GHN tạo đơn fail

Kiểm tra:

- token
- shop id
- địa chỉ lấy hàng
- tên ward/district/province có đúng format GHN không

### GHN có shipment nhưng không có event

Kiểm tra:

- refresh flow có chạy thật không
- webhook có tới app không
- payload GHN có field trạng thái mong đợi không

---

## 9. Khuyến nghị làm tiếp

Sau khi bạn cấu hình sandbox thật xong, bước tiếp theo nên là:

1. thêm `application-dev.properties` và `application-prod.properties`
2. viết test cho payment/shipment integration
3. thêm log nghiệp vụ cho callback VNPay và webhook GHN
4. chụp bằng chứng test để đưa vào Sprint 6 / Sprint 7
