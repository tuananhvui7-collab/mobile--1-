# Luồng Cơ Bản Theo Use Case

Tài liệu này viết theo đúng kiểu phân tích hệ thống:

- **Actor**: ai thực hiện
- **Boundary**: màn hình / form / giao diện
- **Control**: lớp điều phối
- **Entity**: thực thể dữ liệu
- **HTTP**: phương thức và đường dẫn
- **Luồng cơ bản**: các bước chính, có method ngắn gọn

---

## U1 - Đăng nhập

- **Actor**: Người dùng
- **Boundary**: `LoginForm`
- **Control**: `AuthControl`
- **Entity**: `User`
- **HTTP**: `POST /login`

### Luồng cơ bản
1. Người dùng gọi `enterCredentials(email, password)` trên `LoginForm`.
2. `LoginForm` gọi `submitLogin(email, password)` sang `AuthControl`.
3. `AuthControl` gọi `findByEmail(email)` trên `User`.
4. `User` đọc dữ liệu từ CSDL bằng `queryUserRole()`.
5. `AuthControl` gọi `validatePassword()` và trả `loginSuccess(role)` hoặc `loginFailed()`.
6. `LoginForm` hiển thị `redirectByRole()` hoặc `showLoginError()`.

## U2 - Đăng xuất

- **Actor**: Người dùng
- **Boundary**: `NavBar`
- **Control**: `AuthControl`
- **Entity**: `Session`
- **HTTP**: `POST /logout`

### Luồng cơ bản
1. Người dùng gọi `clickLogout()` trên `NavBar`.
2. `NavBar` gọi `submitLogout()` sang `AuthControl`.
3. `AuthControl` gọi `clearSession()` trên `Session`.
4. `Session` xóa phiên làm việc.
5. `AuthControl` trả `logoutSuccess()`.
6. `NavBar` gọi `redirectLogin()`.

## U3 - Đăng ký tài khoản

- **Actor**: Khách
- **Boundary**: `RegisterForm`
- **Control**: `AuthControl`
- **Entity**: `User`
- **HTTP**: `POST /register`

### Luồng cơ bản
1. Khách gọi `enterRegistrationData()` trên `RegisterForm`.
2. `RegisterForm` gọi `submitRegister(form)` sang `AuthControl`.
3. `AuthControl` gọi `checkEmail(email)` trên `User`.
4. `User` đọc CSDL bằng `findByEmail(email)`.
5. Nếu email hợp lệ, `AuthControl` gọi `createCustomer()`.
6. `User` lưu dữ liệu bằng `saveCustomer()`.
7. `RegisterForm` hiển thị `registerSuccess()` và `redirectLogin()`.

## U4 - Xem và tìm kiếm sản phẩm

- **Actor**: Khách hàng
- **Boundary**: `ProductPage`
- **Control**: `ProductControl`
- **Entity**: `Product`
- **HTTP**: `GET /products`

### Luồng cơ bản
1. Khách hàng gọi `enterSearchKeyword(keyword)` trên `ProductPage`.
2. `ProductPage` gọi `loadProducts(keyword)` sang `ProductControl`.
3. `ProductControl` gọi `findByKeyword(keyword)` trên `Product`.
4. `Product` đọc CSDL bằng `queryProducts()`.
5. `ProductControl` nhận `productList`.
6. `ProductPage` hiển thị `renderProductList()` hoặc `renderEmptyState()`.

## U5 - Thêm vào giỏ và quản lý giỏ hàng

- **Actor**: Khách hàng
- **Boundary**: `DetailPage`, `CartPage`
- **Control**: `CartControl`
- **Entity**: `Cart`, `CartItem`, `ProductVariant`
- **HTTP**: `POST /cart/add`, `POST /cart/update`

### Luồng cơ bản
1. Khách hàng gọi `selectVariantAndQty(variantId, qty)` trên `DetailPage`.
2. `DetailPage` gọi `submitAddToCart(variantId, qty)` sang `CartControl`.
3. `CartControl` gọi `checkStock(variantId)` trên `ProductVariant`.
4. `ProductVariant` đọc CSDL bằng `readStockAndPrice()`.
5. Nếu còn hàng, `CartControl` gọi `saveCartItem()`.
6. `Cart` lưu CSDL bằng `persistCart()`.
7. `CartPage` hiển thị `addSuccess()` và `redirectCart()`.

## U6 - Thanh toán và đặt hàng

- **Actor**: Khách hàng
- **Boundary**: `CheckoutPage`
- **Control**: `OrderControl`
- **Entity**: `Cart`, `Order`, `OrderItem`
- **HTTP**: `GET /orders/checkout`, `POST /orders/place`

### Luồng cơ bản
1. Khách hàng gọi `openCheckout()` trên `CheckoutPage`.
2. `CheckoutPage` gọi `loadCheckout()` sang `OrderControl`.
3. `OrderControl` gọi `readCurrentCart()` trên `Cart`.
4. `Cart` đọc CSDL bằng `queryCart()`.
5. `OrderControl` trả `showCheckoutConfirm()`.
6. Khách hàng gọi `clickPlaceOrder()`.
7. `CheckoutPage` gọi `submitPlaceOrder()` sang `OrderControl`.
8. `OrderControl` gọi `createOrder()` trên `Order`.
9. `Order` lưu CSDL bằng `saveOrderAndItems()`.
10. `OrderControl` trả `orderCreated()` và `redirectOrderDetail()`.

## U7 - Thanh toán COD hoặc VNPAY

- **Actor**: Khách hàng
- **Boundary**: `PaymentPage`
- **Control**: `PaymentControl`
- **Entity**: `Payment`
- **HTTP**: `GET /payments/{id}`, `POST /payments/{id}/vnpay`

### Luồng cơ bản
1. Khách hàng gọi `selectPaymentMethod()` trên `PaymentPage`.
2. `PaymentPage` gọi `loadPaymentDetail(orderId)` sang `PaymentControl`.
3. `PaymentControl` gọi `findByOrderId(orderId)` trên `Payment`.
4. `Payment` đọc CSDL bằng `queryPayment()`.
5. `PaymentControl` trả `showPaymentStatus()`.
6. Nếu chọn COD, `PaymentPage` gọi `confirmCOD()`.
7. Nếu chọn VNPAY, `PaymentPage` gọi `createVnpayRequest()`.
8. Hệ thống chuyển sang `redirectMockGateway()` hoặc `redirectGateway()`.

## U8 - Theo dõi vận chuyển

- **Actor**: Khách hàng
- **Boundary**: `TrackingPage`
- **Control**: `ShipmentControl`
- **Entity**: `Shipment`
- **HTTP**: `GET /orders/{id}/tracking`

### Luồng cơ bản
1. Khách hàng gọi `openTrackingPage()` trên `TrackingPage`.
2. `TrackingPage` gọi `loadTracking(orderId)` sang `ShipmentControl`.
3. `ShipmentControl` gọi `findByOrderId(orderId)` trên `Shipment`.
4. `Shipment` đọc CSDL bằng `queryShipment()`.
5. `ShipmentControl` gọi `refreshTracking()` tới cổng giao hàng.
6. Nếu có dữ liệu mới, `ShipmentControl` gọi `updateTrackingTimeline()`.
7. `TrackingPage` hiển thị `renderTrackingPage()` và `showTrackingProgress()`.

## U9 - Hủy đơn khi được phép

- **Actor**: Khách hàng
- **Boundary**: `OrderDetail`
- **Control**: `OrderControl`
- **Entity**: `Order`
- **HTTP**: `POST /orders/{id}/cancel`

### Luồng cơ bản
1. Khách hàng gọi `clickCancelOrder()` trên `OrderDetail`.
2. `OrderDetail` gọi `submitCancelOrder(orderId)` sang `OrderControl`.
3. `OrderControl` gọi `checkOrderStatus(orderId)` trên `Order`.
4. `Order` đọc CSDL bằng `queryOrder()`.
5. Nếu đủ điều kiện, `OrderControl` gọi `setCancelled()`.
6. `Order` lưu CSDL bằng `saveChanges()`.
7. `OrderDetail` hiển thị `cancelSuccess()` hoặc `showCancelDenied()`.

## U10 - Đánh giá sản phẩm đã mua

- **Actor**: Khách hàng
- **Boundary**: `ProductDetail`
- **Control**: `ReviewControl`
- **Entity**: `Review`, `Order`, `OrderItem`
- **HTTP**: `GET /reviews/new`, `POST /reviews`

### Luồng cơ bản
1. Khách hàng gọi `openReviewForm()` trên `ProductDetail`.
2. `ProductDetail` gọi `loadReviewForm(productId)` sang `ReviewControl`.
3. `ReviewControl` gọi `checkPurchaseHistory(customerId, productId)` trên `Order`.
4. `Order` đọc CSDL bằng `queryOrders()`.
5. Nếu hợp lệ, `ProductDetail` cho phép `enterReview(rating, comment)`.
6. `ProductDetail` gọi `submitReview()` sang `ReviewControl`.
7. `ReviewControl` gọi `createReviewRecord()` trên `Review`.
8. `Review` lưu CSDL bằng `saveReview()`.

## U11 - Cập nhật thông tin cá nhân và địa chỉ

- **Actor**: Khách hàng
- **Boundary**: `ProfilePage`
- **Control**: `ProfileControl`
- **Entity**: `User`, `Address`
- **HTTP**: `GET /profile`, `POST /profile/update`

### Luồng cơ bản
1. Khách hàng gọi `openProfilePage()` trên `ProfilePage`.
2. `ProfilePage` gọi `loadProfileData()` sang `ProfileControl`.
3. `ProfileControl` gọi `readUserAndAddresses()` trên `User`.
4. `User` đọc CSDL bằng `queryProfileData()`.
5. `ProfilePage` hiển thị `showProfileForm()`.
6. Khách hàng gọi `editProfile()`.
7. `ProfilePage` gọi `updateProfileRequest()` sang `ProfileControl`.
8. `ProfileControl` gọi `saveProfile()` trên `User`.
9. `User` lưu CSDL bằng `saveChanges()`.

## U12 - Tiếp nhận và xử lý đơn hàng

- **Actor**: Nhân viên
- **Boundary**: `StaffOrderPage`
- **Control**: `StaffOrderControl`
- **Entity**: `Order`, `Shipment`
- **HTTP**: `GET /employee/orders`, `POST /employee/orders/{id}/status`

### Luồng cơ bản
1. Nhân viên gọi `openOrderList()` trên `StaffOrderPage`.
2. `StaffOrderPage` gọi `loadStaffOrders()` sang `StaffOrderControl`.
3. `StaffOrderControl` gọi `queryStaffOrders()` trên `Order`.
4. `Order` đọc CSDL bằng `queryOrders()`.
5. `StaffOrderPage` hiển thị `renderOrderList()`.
6. Nhân viên gọi `changeOrderStatus()`.
7. `StaffOrderPage` gọi `submitStatusUpdate()` sang `StaffOrderControl`.
8. `StaffOrderControl` gọi `updateOrderStatusById()` trên `Order`.
9. `Order` lưu CSDL và `Shipment` đồng bộ trạng thái.

## U13 - Quản lý sản phẩm

- **Actor**: Quản lý
- **Boundary**: `ProductAdminPage`
- **Control**: `ProductControl`
- **Entity**: `Product`, `Variant`
- **HTTP**: `GET /admin/products`, `POST /admin/products`

### Luồng cơ bản
1. Quản lý gọi `openProductAdmin()` trên `ProductAdminPage`.
2. `ProductAdminPage` gọi `loadAdminProductList()` sang `ProductControl`.
3. `ProductControl` gọi `queryProductList()` trên `Product`.
4. `Product` đọc CSDL bằng `queryProducts()`.
5. `ProductAdminPage` hiển thị `renderProductList()`.
6. Quản lý gọi `submitProductForm()`.
7. `ProductAdminPage` gọi `saveProductRequest()` sang `ProductControl`.
8. `ProductControl` gọi `saveOrUpdateProductRecord()` trên `Product`.
9. `Product` lưu CSDL bằng `writeProductData()`.

## U14 - Xem báo cáo kinh doanh

- **Actor**: Quản lý
- **Boundary**: `ReportPage`
- **Control**: `ReportControl`
- **Entity**: `DWH`
- **HTTP**: `GET /admin/reports`

### Luồng cơ bản
1. Quản lý gọi `openReportPage()` trên `ReportPage`.
2. `ReportPage` gọi `loadReportPage()` sang `ReportControl`.
3. `ReportControl` gọi `readKpiSummary()` trên `DWH`.
4. `DWH` trả `kpiSnapshot`.
5. Nếu có URL nhúng, `ReportPage` gọi `showDashboard()`.
6. Nếu không có URL, `ReportPage` gọi `showFallback()`.

## U15 - Xử lý phản hồi của khách hàng

- **Actor**: Khách hàng, Nhân viên
- **Boundary**: `FeedbackPage`
- **Control**: `FeedbackControl`
- **Entity**: `Feedback`
- **HTTP**: `POST /feedback`, `GET /employee/feedbacks`

### Luồng cơ bản
1. Khách hàng gọi `submitFeedback()` trên `FeedbackPage`.
2. `FeedbackPage` gọi `createFeedbackRequest()` sang `FeedbackControl`.
3. `FeedbackControl` gọi `createFeedbackRecord()` trên `Feedback`.
4. `Feedback` lưu CSDL bằng `saveFeedback()`.
5. `FeedbackPage` hiển thị `showFeedbackSuccess()`.
6. Nhân viên gọi `openFeedbackList()`.
7. `FeedbackPage` gọi `loadFeedbackList()` sang `FeedbackControl`.
8. `FeedbackControl` gọi `queryFeedbacks()` trên `Feedback`.

## U16 - Xác thực giao dịch thanh toán

- **Actor**: Cổng thanh toán
- **Boundary**: `ReturnPage`
- **Control**: `PaymentControl`
- **Entity**: `Payment`, `Order`
- **HTTP**: `GET /payments/vnpay/return`

### Luồng cơ bản
1. Cổng thanh toán gọi `sendCallback()` sau khi xử lý giao dịch.
2. `ReturnPage` gọi `loadPaymentReturn()` sang `PaymentControl`.
3. `PaymentControl` gọi `readPaymentByCode()` trên `Payment`.
4. `Payment` đọc CSDL bằng `queryPayment()`.
5. `PaymentControl` gọi `checkOrderStatus()` trên `Order`.
6. Nếu hợp lệ, `PaymentControl` gọi `markSuccess()` và `confirmOrder()`.
7. Nếu thất bại, `PaymentControl` gọi `markFailedOrPending()`.
8. `ReturnPage` hiển thị `returnVerificationResult()`.

## U17 - Cập nhật lộ trình vận chuyển

- **Actor**: Cổng giao hàng
- **Boundary**: `TrackPage`
- **Control**: `ShipmentControl`
- **Entity**: `Shipment`
- **HTTP**: `POST /shipments/webhook`

### Luồng cơ bản
1. Cổng giao hàng gọi `refreshShipmentStatus()` hoặc webhook.
2. `TrackPage` / `ShipmentControl` gọi `readShipment()` trên `Shipment`.
3. `Shipment` đọc CSDL bằng `queryShipment()`.
4. Nếu có trạng thái mới, `ShipmentControl` gọi `updateTrackingTimeline()`.
5. `Shipment` lưu CSDL bằng `saveChanges()`.
6. `TrackPage` hiển thị `renderTrackingPage()`.

