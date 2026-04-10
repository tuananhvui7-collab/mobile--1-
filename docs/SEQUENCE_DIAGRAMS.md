# Biểu Đồ Tuần Tự Chi Tiết Theo Use Case

Tài liệu này bám theo đúng tinh thần của **Buổi 3: Thiết kế hệ thống - Biểu đồ tuần tự chi tiết**.

Mỗi use case đều thể hiện các nhóm đối tượng:

- **Actor**: tác nhân bên ngoài
- **Boundary**: màn hình / form / giao diện
- **Control**: lớp điều phối
- **Entity**: lớp dữ liệu / thực thể
- **CSDL**: nơi lưu trữ dữ liệu

Các thông điệp đều viết theo kiểu **method / hành động ngắn**. Với các tương tác web, có thể kèm thêm HTTP verb như `GET` hoặc `POST` để bài rõ hơn nhưng vẫn giữ mức thiết kế.

---

## U1 - Đăng nhập

```mermaid
sequenceDiagram
    actor User as Người dùng
    participant LoginForm as Màn đăng nhập (Boundary)
    participant AuthControl as Điều khiển xác thực (Control)
    participant UserEntity as User (Entity)
    participant DB as CSDL

    User->>LoginForm: enterCredentials(email, password)
    LoginForm->>AuthControl: POST submitLogin(email, password)
    AuthControl->>UserEntity: findByEmail(email)
    UserEntity->>DB: queryUserRole()
    DB-->>UserEntity: userData
    UserEntity-->>AuthControl: userProfile
    AuthControl->>AuthControl: validatePassword()
    alt Hợp lệ
        AuthControl-->>LoginForm: loginSuccess(role)
        LoginForm-->>User: redirectByRole()
    else Không hợp lệ
        AuthControl-->>LoginForm: loginFailed()
        LoginForm-->>User: showLoginError()
    end
```

## U2 - Đăng xuất

```mermaid
sequenceDiagram
    actor User as Người dùng
    participant NavBar as Thanh điều hướng (Boundary)
    participant AuthControl as Điều khiển xác thực (Control)

    User->>NavBar: clickLogout()
    NavBar->>AuthControl: POST submitLogout()
    AuthControl->>AuthControl: clearSession()
    AuthControl-->>NavBar: logoutSuccess()
    NavBar-->>User: redirectLogin()
```

## U3 - Đăng ký tài khoản

```mermaid
sequenceDiagram
    actor Guest as Khách
    participant RegisterForm as Màn đăng ký (Boundary)
    participant AuthControl as Điều khiển xác thực (Control)
    participant UserEntity as User (Entity)
    participant DB as CSDL

    Guest->>RegisterForm: enterRegistrationData()
    RegisterForm->>AuthControl: POST submitRegister(form)
    AuthControl->>UserEntity: checkEmail(email)
    UserEntity->>DB: findByEmail(email)
    DB-->>UserEntity: emailResult
    alt Email đã tồn tại
        AuthControl-->>RegisterForm: showDuplicateEmailError()
        RegisterForm-->>Guest: showError()
    else Email hợp lệ
        AuthControl->>UserEntity: createCustomer()
        UserEntity->>DB: saveCustomer()
        DB-->>UserEntity: saveResult
        UserEntity-->>AuthControl: registerSuccess()
        AuthControl-->>RegisterForm: redirectLogin()
        RegisterForm-->>Guest: showSuccess()
    end
```

## U4 - Xem và tìm kiếm sản phẩm

```mermaid
sequenceDiagram
    actor Customer as Khách hàng
    participant ProductPage as Màn danh sách sản phẩm (Boundary)
    participant ProductControl as Điều khiển sản phẩm (Control)
    participant ProductEntity as Product (Entity)
    participant DB as CSDL

    Customer->>ProductPage: enterSearchKeyword(keyword)
    ProductPage->>ProductControl: GET loadProducts(keyword)
    ProductControl->>ProductEntity: findByKeyword(keyword)
    ProductEntity->>DB: queryProducts()
    DB-->>ProductEntity: productList
    ProductEntity-->>ProductControl: productList
    alt Có kết quả
        ProductControl-->>ProductPage: renderProductList()
        ProductPage-->>Customer: showProducts()
    else Không có kết quả
        ProductControl-->>ProductPage: renderEmptyState()
        ProductPage-->>Customer: showNoResultMessage()
    end
```

## U5 - Thêm vào giỏ và quản lý giỏ hàng

```mermaid
sequenceDiagram
    actor Customer as Khách hàng
    participant DetailPage as Màn chi tiết sản phẩm (Boundary)
    participant CartPage as Màn giỏ hàng (Boundary)
    participant CartControl as Điều khiển giỏ hàng (Control)
    participant CartEntity as Cart / CartItem (Entity)
    participant VariantEntity as ProductVariant (Entity)
    participant DB as CSDL

    Customer->>DetailPage: selectVariantAndQty(variantId, qty)
    DetailPage->>CartControl: POST submitAddToCart(variantId, qty)
    CartControl->>VariantEntity: checkStock(variantId)
    VariantEntity->>DB: readStockAndPrice()
    DB-->>VariantEntity: variantData
    alt Còn hàng
        CartControl->>CartEntity: saveCartItem()
        CartEntity->>DB: persistCart()
        DB-->>CartEntity: saveResult
        CartControl-->>CartPage: addSuccess()
        CartPage-->>Customer: redirectCart()
    else Hết hàng
        CartControl-->>DetailPage: showStockError()
        DetailPage-->>Customer: showWarning()
    end

    Customer->>CartPage: adjustCartItem()
    CartPage->>CartControl: POST submitUpdateItem()
    CartControl->>CartEntity: updateItem()
    CartEntity->>DB: saveChanges()
    DB-->>CartEntity: updated
    CartControl-->>CartPage: renderCart()
```

## U6 - Thanh toán và đặt hàng

```mermaid
sequenceDiagram
    actor Customer as Khách hàng
    participant CheckoutPage as Màn checkout (Boundary)
    participant OrderControl as Điều khiển đặt hàng (Control)
    participant CartEntity as Cart / CartItem (Entity)
    participant OrderEntity as Order / OrderItem (Entity)
    participant DB as CSDL

    Customer->>CheckoutPage: openCheckout()
    CheckoutPage->>OrderControl: GET loadCheckout()
    OrderControl->>CartEntity: readCurrentCart()
    CartEntity->>DB: queryCart()
    DB-->>CartEntity: cartData
    CartEntity-->>OrderControl: checkoutData
    OrderControl-->>CheckoutPage: showCheckoutConfirm()

    Customer->>CheckoutPage: clickPlaceOrder()
    CheckoutPage->>OrderControl: POST submitPlaceOrder()
    OrderControl->>OrderEntity: createOrder()
    OrderEntity->>DB: saveOrderAndItems()
    DB-->>OrderEntity: saveResult
    OrderEntity-->>OrderControl: orderCreated()
    OrderControl-->>CheckoutPage: redirectOrderDetail()
```

## U7 - Thanh toán COD hoặc VNPAY

```mermaid
sequenceDiagram
    actor Customer as Khách hàng
    participant PaymentPage as Màn thanh toán (Boundary)
    participant PaymentControl as Điều khiển thanh toán (Control)
    participant PaymentEntity as Payment (Entity)
    participant Gateway as Cổng thanh toán
    participant DB as CSDL

    Customer->>PaymentPage: selectPaymentMethod()
    PaymentPage->>PaymentControl: GET loadPaymentDetail(orderId)
    PaymentControl->>PaymentEntity: findByOrderId(orderId)
    PaymentEntity->>DB: queryPayment()
    DB-->>PaymentEntity: paymentData
    PaymentEntity-->>PaymentControl: paymentData
    PaymentControl-->>PaymentPage: showPaymentStatus()

    alt COD
        Customer->>PaymentPage: chooseCOD()
        PaymentPage->>PaymentControl: POST confirmCOD()
        PaymentControl->>PaymentEntity: markCOD()
        PaymentEntity->>DB: saveChanges()
        DB-->>PaymentEntity: updated
        PaymentControl-->>PaymentPage: paymentConfirmed()
    else VNPAY
        Customer->>PaymentPage: chooseVNPAY()
        PaymentPage->>PaymentControl: POST createVnpayRequest()
        PaymentControl->>Gateway: buildPaymentRequest()
        alt mock mode
            Gateway-->>PaymentPage: redirectMockGateway()
        else sandbox thật
            Gateway-->>PaymentPage: redirectGateway()
        end
    end
```

## U8 - Theo dõi vận chuyển

```mermaid
sequenceDiagram
    actor Customer as Khách hàng
    participant TrackingPage as Màn theo dõi (Boundary)
    participant ShipmentControl as Điều khiển vận chuyển (Control)
    participant ShipmentEntity as Shipment (Entity)
    participant GhnGateway as Cổng giao hàng
    participant DB as CSDL

    Customer->>TrackingPage: openTrackingPage()
    TrackingPage->>ShipmentControl: GET loadTracking(orderId)
    ShipmentControl->>ShipmentEntity: findByOrderId(orderId)
    ShipmentEntity->>DB: queryShipment()
    DB-->>ShipmentEntity: shipmentData
    ShipmentEntity-->>ShipmentControl: shipmentData
    ShipmentControl->>GhnGateway: refreshTracking()
    alt GHN có dữ liệu mới
        GhnGateway-->>ShipmentControl: newStatus
        ShipmentControl->>ShipmentEntity: updateTrackingTimeline()
        ShipmentEntity->>DB: saveChanges()
        DB-->>ShipmentEntity: updated
    else GHN lỗi / chưa cấu hình
        GhnGateway-->>ShipmentControl: fallbackInternal()
    end
    ShipmentControl-->>TrackingPage: renderTrackingPage()
    TrackingPage-->>Customer: showTrackingProgress()
```

## U9 - Hủy đơn khi được phép

```mermaid
sequenceDiagram
    actor Customer as Khách hàng
    participant OrderDetail as Màn chi tiết đơn (Boundary)
    participant OrderControl as Điều khiển đặt hàng (Control)
    participant OrderEntity as Order (Entity)
    participant DB as CSDL

    Customer->>OrderDetail: clickCancelOrder()
    OrderDetail->>OrderControl: POST submitCancelOrder(orderId)
    OrderControl->>OrderEntity: checkOrderStatus(orderId)
    OrderEntity->>DB: queryOrder()
    DB-->>OrderEntity: orderData
    alt Đơn đủ điều kiện hủy
        OrderControl->>OrderEntity: setCancelled()
        OrderEntity->>DB: saveChanges()
        DB-->>OrderEntity: updated
        OrderControl-->>OrderDetail: cancelSuccess()
        OrderDetail-->>Customer: showUpdatedStatus()
    else Không được hủy
        OrderControl-->>OrderDetail: showCancelDenied()
        OrderDetail-->>Customer: showWarning()
    end
```

## U10 - Đánh giá sản phẩm đã mua

```mermaid
sequenceDiagram
    actor Customer as Khách hàng
    participant ProductDetail as Màn chi tiết sản phẩm (Boundary)
    participant ReviewControl as Điều khiển đánh giá (Control)
    participant ReviewEntity as Review (Entity)
    participant OrderEntity as Order / OrderItem (Entity)
    participant DB as CSDL

    Customer->>ProductDetail: openReviewForm()
    ProductDetail->>ReviewControl: GET loadReviewForm(productId)
    ReviewControl->>OrderEntity: checkPurchaseHistory(customerId, productId)
    OrderEntity->>DB: queryOrders()
    DB-->>OrderEntity: purchaseResult
    alt Đã mua và đã giao
        ReviewControl-->>ProductDetail: allowReview()
        Customer->>ProductDetail: enterReview(rating, comment)
        ProductDetail->>ReviewControl: POST submitReview()
        ReviewControl->>ReviewEntity: createReviewRecord()
        ReviewEntity->>DB: saveReview()
        DB-->>ReviewEntity: saveResult
        ReviewControl-->>ProductDetail: refreshAverageRating()
    else Chưa đủ điều kiện
        ReviewControl-->>ProductDetail: denyReview()
        ProductDetail-->>Customer: hideReviewForm()
    end
```

## U11 - Cập nhật thông tin cá nhân và địa chỉ

```mermaid
sequenceDiagram
    actor Customer as Khách hàng
    participant ProfilePage as Màn hồ sơ (Boundary)
    participant ProfileControl as Điều khiển hồ sơ (Control)
    participant UserEntity as User / Address (Entity)
    participant DB as CSDL

    Customer->>ProfilePage: openProfilePage()
    ProfilePage->>ProfileControl: GET loadProfileData()
    ProfileControl->>UserEntity: readUserAndAddresses()
    UserEntity->>DB: queryProfileData()
    DB-->>UserEntity: profileData
    UserEntity-->>ProfileControl: profileData
    ProfileControl-->>ProfilePage: showProfileForm()

    Customer->>ProfilePage: editProfile()
    ProfilePage->>ProfileControl: POST updateProfileRequest()
    ProfileControl->>UserEntity: saveProfile()
    UserEntity->>DB: saveChanges()
    DB-->>UserEntity: updated
    ProfileControl-->>ProfilePage: showUpdateSuccess()
```

## U12 - Tiếp nhận và xử lý đơn hàng

```mermaid
sequenceDiagram
    actor Staff as Nhân viên
    participant StaffOrderPage as Màn đơn hàng nhân viên (Boundary)
    participant StaffOrderControl as Điều khiển đơn hàng (Control)
    participant OrderEntity as Order (Entity)
    participant ShipmentEntity as Shipment (Entity)
    participant DB as CSDL

    Staff->>StaffOrderPage: openOrderList()
    StaffOrderPage->>StaffOrderControl: GET loadStaffOrders()
    StaffOrderControl->>OrderEntity: queryStaffOrders()
    OrderEntity->>DB: queryOrders()
    DB-->>OrderEntity: orderList
    OrderEntity-->>StaffOrderControl: orderList
    StaffOrderControl-->>StaffOrderPage: renderOrderList()

    Staff->>StaffOrderPage: changeOrderStatus()
    StaffOrderPage->>StaffOrderControl: POST submitStatusUpdate()
    StaffOrderControl->>OrderEntity: updateOrderStatusById()
    OrderEntity->>DB: saveOrder()
    DB-->>OrderEntity: updated
    StaffOrderControl->>ShipmentEntity: syncShipmentStatus()
```

## U13 - Quản lý sản phẩm

```mermaid
sequenceDiagram
    actor Manager as Quản lý
    participant ProductAdminPage as Màn quản lý sản phẩm (Boundary)
    participant ProductControl as Điều khiển sản phẩm (Control)
    participant ProductEntity as Product / Variant (Entity)
    participant DB as CSDL

    Manager->>ProductAdminPage: openProductAdmin()
    ProductAdminPage->>ProductControl: GET loadAdminProductList()
    ProductControl->>ProductEntity: queryProductList()
    ProductEntity->>DB: queryProducts()
    DB-->>ProductEntity: productList
    ProductEntity-->>ProductControl: productList
    ProductControl-->>ProductAdminPage: renderProductList()

    alt Tạo / sửa / xóa
        Manager->>ProductAdminPage: POST submitProductForm()
        ProductAdminPage->>ProductControl: saveProductRequest()
        ProductControl->>ProductEntity: saveOrUpdateProductRecord()
        ProductEntity->>DB: writeProductData()
        DB-->>ProductEntity: updated
    end
```

## U14 - Xem báo cáo kinh doanh

```mermaid
sequenceDiagram
    actor Manager as Quản lý
    participant ReportPage as Màn báo cáo (Boundary)
    participant ReportControl as Điều khiển báo cáo (Control)
    participant DWH as Kho dữ liệu
    participant Metabase as Dashboard BI

    Manager->>ReportPage: openReportPage()
    ReportPage->>ReportControl: GET loadReportPage()
    ReportControl->>DWH: readKpiSummary()
    DWH-->>ReportControl: kpiSnapshot
    alt Có URL nhúng
        ReportControl-->>Metabase: renderDashboardIframe()
        Metabase-->>ReportPage: showDashboard()
    else Không có URL
        ReportControl-->>ReportPage: showFallback()
    end
```

## U15 - Xử lý phản hồi của khách hàng

```mermaid
sequenceDiagram
    actor Customer as Khách hàng
    actor Staff as Nhân viên
    participant FeedbackPage as Màn phản hồi (Boundary)
    participant FeedbackControl as Điều khiển phản hồi (Control)
    participant FeedbackEntity as Feedback (Entity)
    participant DB as CSDL

    Customer->>FeedbackPage: submitFeedback()
    FeedbackPage->>FeedbackControl: POST createFeedbackRequest()
    FeedbackControl->>FeedbackEntity: createFeedbackRecord()
    FeedbackEntity->>DB: saveFeedback()
    DB-->>FeedbackEntity: saveResult
    FeedbackControl-->>FeedbackPage: showFeedbackSuccess()

    Staff->>FeedbackPage: openFeedbackList()
    FeedbackPage->>FeedbackControl: GET loadFeedbackList()
    FeedbackControl->>FeedbackEntity: queryFeedbacks()
    FeedbackEntity->>DB: queryFeedback()
    DB-->>FeedbackEntity: feedbackList
    FeedbackEntity-->>FeedbackControl: feedbackList
    FeedbackControl-->>FeedbackPage: showFeedbackProcessing()
```

## U16 - Xác thực giao dịch thanh toán

```mermaid
sequenceDiagram
    actor Gateway as Cổng thanh toán
    participant ReturnPage as Màn trả kết quả (Boundary)
    participant PaymentControl as Điều khiển thanh toán (Control)
    participant PaymentEntity as Payment (Entity)
    participant OrderEntity as Order (Entity)
    participant DB as CSDL

    Gateway->>ReturnPage: sendCallback()
    ReturnPage->>PaymentControl: GET loadPaymentReturn()
    PaymentControl->>PaymentEntity: readPaymentByCode()
    PaymentEntity->>DB: queryPayment()
    DB-->>PaymentEntity: paymentData
    PaymentControl->>OrderEntity: checkOrderStatus()
    OrderEntity->>DB: queryOrder()
    DB-->>OrderEntity: orderData
    alt Chữ ký hợp lệ và thành công
        PaymentControl->>PaymentEntity: markSuccess()
        PaymentEntity->>DB: saveChanges()
        PaymentControl->>OrderEntity: confirmOrder()
        OrderEntity->>DB: saveChanges()
    else Thất bại / sai chữ ký
        PaymentControl->>PaymentEntity: markFailedOrPending()
        PaymentEntity->>DB: saveChanges()
    end
    PaymentControl-->>ReturnPage: returnVerificationResult()
```

## U17 - Cập nhật lộ trình vận chuyển

```mermaid
sequenceDiagram
    actor GhnGateway as Cổng giao hàng
    participant TrackPage as Màn theo dõi (Boundary)
    participant ShipmentControl as Điều khiển vận chuyển (Control)
    participant ShipmentEntity as Shipment (Entity)
    participant DB as CSDL

    GhnGateway->>ShipmentControl: refreshShipmentStatus()
    ShipmentControl->>ShipmentEntity: readShipment()
    ShipmentEntity->>DB: queryShipment()
    DB-->>ShipmentEntity: shipmentData
    alt Có trạng thái mới
        ShipmentControl->>ShipmentEntity: updateTrackingTimeline()
        ShipmentEntity->>DB: saveChanges()
        DB-->>ShipmentEntity: updated
    else Không có phản hồi
        ShipmentControl->>ShipmentControl: useInternalFallback()
    end
    ShipmentControl-->>TrackPage: renderTrackingPage()
```

---

## Tự Kiểm Tra

Đối chiếu với bất kỳ use case nào trong tài liệu:

- Có Actor chưa?
- Có Boundary chưa?
- Có Control chưa?
- Có Entity chưa?
- Có method / hành động rõ ràng chưa?
- Luồng chính và luồng phụ đã được thể hiện bằng `alt` / `else` chưa?

Nếu có đủ, thì biểu đồ tuần tự đã đạt yêu cầu của Buổi 3.
