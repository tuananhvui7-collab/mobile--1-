# Luồng Code PhoneShop

Tài liệu này là bản sequence để học **luồng code thật** của PhoneShop.

Nó giống hai file sequence trước ở chỗ:

- có **Actor**
- có **Boundary**
- có **Control**
- có **Entity**
- có **luồng từng bước**

Khác ở chỗ:

- nội dung bám sát **class / method thật** trong code
- không viết theo kiểu phân tích hệ thống chung chung
- không cần ghi `GET` / `POST`

---

## 1. U1 - Đăng nhập

- **Actor**: Người dùng
- **Boundary**: `AuthController`, `auth/login.html`
- **Control**: `CustomUserDetailsService`, `SecurityFilterChain`
- **Entity**: `User`, `Role`, `UserRepository`, `RoleRepository`

### Luồng code
1. Người dùng mở `AuthController.showLogin()`.
2. `AuthController` trả về view `auth/login`.
3. Người dùng gửi form đăng nhập.
4. Spring Security gọi `CustomUserDetailsService.loadUserByUsername(email)`.
5. `CustomUserDetailsService` gọi `UserRepository.findByEmail(email)`.
6. `UserRepository` lấy `User` từ CSDL.
7. `CustomUserDetailsService` đọc `Role` của user.
8. Spring Security so khớp mật khẩu.
9. Nếu đúng, hệ thống redirect theo role.
10. Nếu sai, hệ thống trả lỗi về form login.

---

## 2. U2 - Đăng xuất

- **Actor**: Người dùng
- **Boundary**: `fragments/layout.html`
- **Control**: Spring Security logout handler
- **Entity**: Session

### Luồng code
1. Người dùng bấm nút logout trên navbar.
2. Layout gửi form logout.
3. Security handler xóa session hiện tại.
4. Hệ thống redirect về trang login.

---

## 3. U4 - Xem và tìm kiếm sản phẩm

- **Actor**: Khách hàng
- **Boundary**: `ProductController`, `product/list.html`, `product/detail.html`
- **Control**: `ProductService`
- **Entity**: `Product`, `ProductVariant`, `Category`, `Review`

### Luồng code
1. Khách hàng mở `ProductController.listProducts()`.
2. `ProductController` gọi `ProductService.searchProducts()`.
3. `ProductService` gọi `ProductRepository`.
4. `ProductRepository` lấy danh sách sản phẩm từ CSDL.
5. `ProductController` đẩy dữ liệu sang `product/list.html`.
6. Khi mở chi tiết, `ProductController.productDetail()` được gọi.
7. `ProductService.getActiveProductDetailById()` load `Product`.
8. `ProductService` load thêm `ProductVariant`, `Category`, `Review`.
9. View `product/detail.html` render trang chi tiết.

---

## 4. U5 - Thêm vào giỏ và quản lý giỏ hàng

- **Actor**: Khách hàng
- **Boundary**: `CartController`, `product/detail.html`, `cart/view.html`
- **Control**: `CartService`
- **Entity**: `Cart`, `CartItem`, `ProductVariant`

### Luồng code
1. Khách hàng chọn biến thể trên `product/detail.html`.
2. View gọi `CartController.addToCart()`.
3. `CartController` gọi `CartService.addToCart()`.
4. `CartService` gọi `ProductVariantRepository.findById()`.
5. `ProductVariantRepository` trả biến thể và tồn kho.
6. `CartService` gọi `CartRepository.findByCustomerEmail()`.
7. Nếu chưa có giỏ thì tạo `Cart`.
8. `CartService` tạo hoặc cập nhật `CartItem`.
9. `CartItemRepository` lưu dữ liệu vào CSDL.
10. `CartController` redirect về `cart/view.html`.
11. Khi chỉnh số lượng, `CartController.updateItem()` hoặc `CartController.removeItem()` được gọi.
12. `CartService` cập nhật `CartItem` và lưu lại.

---

## 5. U6 - Thanh toán và đặt hàng

- **Actor**: Khách hàng
- **Boundary**: `OrderController`, `order/checkout.html`, `order/detail.html`
- **Control**: `OrderService`, `CartService`, `PaymentService`, `ShipmentService`
- **Entity**: `Order`, `OrderItem`, `Cart`, `Payment`, `Shipment`

### Luồng code
1. Khách hàng mở trang checkout.
2. `OrderController.checkout()` gọi `CartService.getCartByCustomerEmail()`.
3. `CartService` lấy `Cart` và `CartItem` từ CSDL.
4. `OrderController` trả dữ liệu sang `order/checkout.html`.
5. Khách hàng bấm đặt hàng.
6. `OrderController.placeOrder()` gọi `OrderService.placeOrder()`.
7. `OrderService` tạo `Order`.
8. `OrderService` tạo các `OrderItem`.
9. `OrderRepository` lưu đơn hàng.
10. `PaymentService.createPayment()` tạo bản ghi thanh toán.
11. `ShipmentService.ensureShipmentForOrder()` tạo bản ghi vận chuyển.
12. `CartService.clearCart()` xóa giỏ sau khi đặt hàng.
13. `OrderController` redirect sang `order/detail.html`.

---

## 6. U7 - Thanh toán COD hoặc VNPAY

- **Actor**: Khách hàng
- **Boundary**: `PaymentController`, `payment/detail.html`, `payment/mock.html`, `payment/return.html`
- **Control**: `PaymentService`, `VnpayService`
- **Entity**: `Payment`, `Order`

### Luồng code
1. Khách hàng mở trang thanh toán.
2. `PaymentController.paymentDetail()` gọi `PaymentService.getPaymentForCustomer()`.
3. `PaymentService` lấy `Payment` từ `PaymentRepository`.
4. View `payment/detail.html` hiển thị trạng thái thanh toán.
5. Nếu chọn COD, `PaymentController.confirmPayment()` gọi `PaymentService.confirmPayment()`.
6. `PaymentService` cập nhật trạng thái payment.
7. Nếu chọn VNPAY, `PaymentController.redirectToVnpay()` gọi `VnpayService.buildPaymentUrl()`.
8. Nếu ở mock mode, `PaymentController.mockGateway()` trả `payment/mock.html`.
9. Khi callback quay về, `PaymentController.vnpayReturn()` gọi `VnpayService.verifyReturn()`.
10. `PaymentService.confirmPayment()` cập nhật payment.
11. `Order` được xác nhận nếu thanh toán thành công.

---

## 7. U8 - Theo dõi vận chuyển

- **Actor**: Khách hàng
- **Boundary**: `ShipmentController`, `shipment/tracking.html`
- **Control**: `ShipmentService`, `GhnClient`
- **Entity**: `Shipment`, `ShipmentTrackingEvent`

### Luồng code
1. Khách hàng mở màn theo dõi đơn.
2. `ShipmentController.tracking()` gọi `ShipmentService.getShipmentForCustomer()`.
3. `ShipmentService` đọc `ShipmentRepository`.
4. `ShipmentController` trả dữ liệu sang `shipment/tracking.html`.
5. Khi bấm làm mới, `ShipmentController.refresh()` gọi `ShipmentService.refreshFromGhn()`.
6. `ShipmentService` gọi `GhnClient.fetchStatus()`.
7. Nếu GHN trả dữ liệu, `ShipmentService` cập nhật `Shipment`.
8. `ShipmentTrackingEventRepository` lưu timeline mới.
9. Nếu GHN lỗi, `ShipmentService` dùng fallback nội bộ.

---

## 8. U9 - Hủy đơn khi được phép

- **Actor**: Khách hàng
- **Boundary**: `OrderController`, `order/detail.html`
- **Control**: `OrderService`
- **Entity**: `Order`, `Shipment`

### Luồng code
1. Khách hàng bấm hủy đơn ở trang chi tiết.
2. `OrderController.cancelOrder()` được gọi.
3. `OrderService.cancelOrderByCustomerEmail()` kiểm tra trạng thái đơn.
4. Nếu hợp lệ, `Order` đổi sang `CANCELLED`.
5. `OrderRepository` lưu trạng thái mới.
6. `ShipmentService` cập nhật shipment nếu cần.
7. View hiển thị thông báo hủy thành công.
8. Nếu không hợp lệ, view báo không thể hủy.

---

## 9. U10 - Đánh giá sản phẩm đã mua

- **Actor**: Khách hàng
- **Boundary**: `ReviewController`, `product/detail.html`
- **Control**: `ReviewService`
- **Entity**: `Review`, `Order`, `OrderItem`, `Product`

### Luồng code
1. Khách hàng mở trang chi tiết sản phẩm.
2. `ReviewService.canReviewProduct()` kiểm tra đã mua chưa.
3. `ReviewController` quyết định có hiện form review hay không.
4. Khách hàng gửi đánh giá.
5. `ReviewController` gọi `ReviewService.saveReview()`.
6. `ReviewRepository` lưu review.
7. Trang chi tiết được refresh để hiện rating trung bình mới.

---

## 10. U11 - Cập nhật thông tin cá nhân và địa chỉ

- **Actor**: Khách hàng
- **Boundary**: `ProfileController`, `profile/edit.html`
- **Control**: `CustomerService`, `AddressController`, `ProfileControl`
- **Entity**: `User`, `Address`

### Luồng code
1. Khách hàng mở trang hồ sơ.
2. `ProfileController` gọi service load user và address.
3. `UserRepository` và `AddressRepository` trả dữ liệu.
4. View hiển thị form cập nhật.
5. Khách hàng lưu thay đổi.
6. `CustomerService` cập nhật `User`.
7. `AddressController` xử lý thêm/sửa địa chỉ.
8. `AddressRepository` lưu địa chỉ.

---

## 11. U12 - Tiếp nhận và xử lý đơn hàng

- **Actor**: Nhân viên
- **Boundary**: `StaffOrderController`, `staff/order/list.html`, `staff/order/detail.html`
- **Control**: `OrderService`
- **Entity**: `Order`, `Shipment`, `Payment`

### Luồng code
1. Nhân viên mở danh sách đơn.
2. `StaffOrderController.list()` gọi `OrderService.getAllOrdersForStaff()`.
3. `OrderRepository` trả danh sách đơn.
4. Nhân viên mở chi tiết đơn.
5. `StaffOrderController.detail()` gọi `OrderService.getOrderForStaff()`.
6. Nhân viên chuyển trạng thái.
7. `OrderService.receiveOrderForStaff()` hoặc `advanceOrderStatusForStaff()` được gọi.
8. `OrderRepository` lưu trạng thái mới.
9. `ShipmentService` đồng bộ shipment theo trạng thái đơn.

---

## 12. U13 - Quản lý sản phẩm

- **Actor**: Quản lý
- **Boundary**: `ManagerController`, `manager/product/list.html`
- **Control**: `ManagerService`
- **Entity**: `Product`, `ProductVariant`, `Category`

### Luồng code
1. Quản lý mở danh sách sản phẩm.
2. `ManagerController.list()` gọi `ManagerService.getAllProducts()`.
3. `ProductRepository` trả danh sách sản phẩm.
4. Quản lý mở form tạo/sửa.
5. `ManagerService.loadProductForm()` hoặc `newProductForm()` được gọi.
6. Quản lý lưu sản phẩm.
7. `ManagerService.saveProduct()` ghi `Product` và `ProductVariant`.
8. `ManagerService.softDeleteProduct()` hoặc `restoreProduct()` cập nhật trạng thái.

---

## 13. U14 - Xem báo cáo kinh doanh

- **Actor**: Quản lý
- **Boundary**: `ReportController`, `admin/reports.html`
- **Control**: `ReportService`
- **Entity**: `Order`, `Product`, `ProductVariant`, `ReportPeriodPoint`, `ManagerReportView`

### Luồng code
1. Quản lý mở trang báo cáo.
2. `ReportController.report()` gọi `ReportService.getManagerReport(period)`.
3. `ReportService` đọc `OrderRepository.findAllByOrderByCreatedAtDesc()`.
4. `ReportService` đọc `ProductRepository.findAll()`.
5. `ReportService` đọc `ProductVariantRepository.findAll()`.
6. `ReportService` tính summary, period points, top products, stock alerts.
7. `ReportController` đưa dữ liệu vào `admin/reports.html`.
8. Nếu có URL BI, view nhúng dashboard Metabase.
9. Nếu cần xuất file, `ReportController.export()` gọi `ReportService.exportManagerReportExcel()`.

---

## 14. U15 - Xử lý phản hồi của khách hàng

- **Actor**: Khách hàng, Nhân viên
- **Boundary**: `FeedbackController`, `employee/feedback/list.html`
- **Control**: `FeedbackService`
- **Entity**: `Feedback`, `Customer`, `Employee`

### Luồng code
1. Khách hàng gửi phản hồi.
2. `FeedbackController` gọi `FeedbackService.createFeedback()`.
3. `FeedbackRepository` lưu `Feedback`.
4. Nhân viên mở danh sách phản hồi.
5. `FeedbackService.getPendingFeedbacks()` lấy các phản hồi chưa xử lý.
6. Nhân viên nhận xử lý hoặc resolve.
7. `FeedbackService.assignToMe()` hoặc `resolveFeedback()` cập nhật dữ liệu.

---

## 15. U16 - Xác thực giao dịch thanh toán

- **Actor**: Cổng thanh toán
- **Boundary**: `PaymentController`, `payment/return.html`
- **Control**: `VnpayService`, `PaymentService`
- **Entity**: `Payment`, `Order`

### Luồng code
1. Cổng thanh toán trả callback.
2. `PaymentController.vnpayReturn()` nhận request.
3. `VnpayService.verifyReturn()` kiểm tra chữ ký và tham số.
4. `PaymentService.confirmPayment()` cập nhật trạng thái payment.
5. `Order` được xác nhận nếu thanh toán thành công.
6. View `payment/return.html` hiển thị kết quả.

---

## 16. U17 - Cập nhật lộ trình vận chuyển

- **Actor**: Cổng giao hàng
- **Boundary**: `GhnWebhookController`, `shipment/tracking.html`
- **Control**: `ShipmentService`, `GhnClient`
- **Entity**: `Shipment`, `ShipmentTrackingEvent`

### Luồng code
1. GHN gửi webhook hoặc hệ thống refresh trạng thái.
2. `GhnWebhookController` hoặc `ShipmentController.refresh()` nhận dữ liệu.
3. `ShipmentService.processGhnWebhook()` hoặc `refreshFromGhn()` xử lý payload.
4. `GhnClient` đọc trạng thái từ cổng giao hàng.
5. `ShipmentRepository` lưu trạng thái mới.
6. `ShipmentTrackingEventRepository` lưu timeline.
7. View tracking hiển thị tiến trình mới.

---

## 17. Cách học file này

Mỗi khi mở một file code, bạn chỉ cần hỏi:

- Method này được gọi từ đâu?
- Nó gọi sang service nào?
- Service nào chạm repository nào?
- Repository nào chạm entity nào?
- Sau cùng view nào được render?

Chỉ cần theo được chuỗi đó là bạn sẽ hiểu luồng code rất nhanh.
