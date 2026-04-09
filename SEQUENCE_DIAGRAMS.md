# Biểu Đồ Tuần Tự Chi Tiết Theo Use Case

Tài liệu này bám theo đúng mẫu **Buổi 3: Thiết kế hệ thống - Biểu đồ tuần tự chi tiết**.

Mỗi use case đều thể hiện đủ các nhóm đối tượng:

- **Actor**: tác nhân bên ngoài
- **Boundary**: lớp giao diện / form / page
- **Control**: controller / service điều phối
- **Entity**: entity / dữ liệu / database

---

## U1 - Đăng nhập

```mermaid
sequenceDiagram
    actor User as Người dùng
    participant LoginForm as LoginForm (Boundary)
    participant AuthController as AuthenticationController (Control)
    participant Security as Khối xác thực (Control)
    participant UserEntity as User (Entity)
    participant DB as CSDL

    User->>LoginForm: enterCredentials(email, password)
    LoginForm->>AuthController: authenticate(email, password)
    AuthController->>Security: validateCredentials()
    Security->>UserEntity: findUserByEmail(email)
    UserEntity->>DB: queryUserAndRole()
    DB-->>UserEntity: userData
    UserEntity-->>Security: UserDetails
    Security-->>AuthController: authenticationResult
    alt Đăng nhập addSuccess()
        AuthController-->>LoginForm: redirectByRole()
        LoginForm-->>User: showRolePage()
    else Sai thông tin đăng nhập
        AuthController-->>LoginForm: showLoginError()
        LoginForm-->>User: Hiển thị thông báo lỗi
    end
```

## U2 - Đăng xuất

```mermaid
sequenceDiagram
    actor User as Người dùng
    participant NavBar as NavBar (Boundary)
    participant LogoutController as LogoutHandler (Control)
    participant Security as Khối xác thực (Control)

    User->>NavBar: clickLogout()
    NavBar->>LogoutController: logout()
    LogoutController->>Security: destroySession()
    Security-->>LogoutController: Session đã hủy
    LogoutController-->>NavBar: redirectLogin()
    NavBar-->>User: showLoginPage()
```

## U3 - Đăng ký tài khoản

```mermaid
sequenceDiagram
    actor Guest as Khách
    participant RegisterForm as RegisterForm (Boundary)
    participant AuthController as AuthenticationController (Control)
    participant AuthService as AuthService (Control)
    participant UserEntity as User (Entity)
    participant DB as CSDL

    Guest->>RegisterForm: enterRegistrationData()
    RegisterForm->>AuthController: register(form)
    AuthController->>AuthService: createCustomerAccount(form)
    AuthService->>UserEntity: checkEmail(email)
    UserEntity->>DB: findByEmail(email)
    DB-->>UserEntity: emailCheckResult
    alt Email đã tồn tại
        AuthService-->>RegisterForm: showDuplicateEmailError()
        RegisterForm-->>Guest: Hiển thị lỗi
    else Email hợp lệ
        AuthService->>UserEntity: createCustomer()
        UserEntity->>DB: saveCustomer()
        DB-->>UserEntity: Đã lưu
        AuthService-->>RegisterForm: registerSuccess()
        RegisterForm-->>Guest: redirectLogin()
    end
```

## U4 - Xem và tìm kiếm sản phẩm

```mermaid
sequenceDiagram
    actor Customer as Khách hàng
    participant ProductPage as ProductListPage (Boundary)
    participant ProductController as ProductController (Control)
    participant productListervice as productListervice (Control)
    participant ProductEntity as Product (Entity)
    participant DB as CSDL

    Customer->>ProductPage: loadSearchPage(keyword)
    ProductPage->>ProductController: loadproductList(keyword)
    ProductController->>productListervice: searchproductList(keyword, filter)
    productListervice->>ProductEntity: findproductListByKeyword()
    ProductEntity->>DB: queryproductList()
    DB-->>ProductEntity: productList
    ProductEntity-->>productListervice: listOfproductList
    alt Có emailCheckResult
        productListervice-->>ProductController: returnproductList()
        ProductController-->>ProductPage: Render emailCheckResult
        ProductPage-->>Customer: showproductList()
    else Không có emailCheckResult
        productListervice-->>ProductController: emptyProductList
        ProductController-->>ProductPage: renderEmptyState()
        ProductPage-->>Customer: showNoResultMessage()
    end
```

## U5 - Thêm vào giỏ và quản lý giỏ hàng

```mermaid
sequenceDiagram
    actor Customer as Khách hàng
    participant DetailPage as ProductDetailPage (Boundary)
    participant CartPage as CartPage (Boundary)
    participant CartController as CartController (Control)
    participant CartService as CartService (Control)
    participant CartEntity as Cart / CartItem (Entity)
    participant ProductVariant as ProductVariant (Entity)
    participant DB as CSDL

    Customer->>DetailPage: selectVariantAndQty(variantId, qty)
    DetailPage->>CartController: addToCartRequest()
    CartController->>CartService: addToCart(variantId, qty)
    CartService->>ProductVariant: checkStock()
    ProductVariant->>DB: readStockAndPrice()
    DB-->>ProductVariant: variantData
    alt Còn hàng
        CartService->>CartEntity: saveCartItem()
        CartEntity->>DB: persistCart()
        DB-->>CartEntity: Đã lưu
        CartService-->>CartController: addSuccess()
        CartController-->>CartPage: redirectCart()
        CartPage-->>Customer: showCart()
    else Hết hàng
        CartService-->>DetailPage: showStockError()
        DetailPage-->>Customer: showWarning()
    end

    Customer->>CartPage: adjustCartItem()
    CartPage->>CartController: updateOrDeleteItem()
    CartController->>CartService: updateCartItem()
    CartService->>CartEntity: updateItem()
    CartEntity->>DB: saveChanges()
    DB-->>CartEntity: updated()
    CartService-->>CartPage: renderCart()
```

## U6 - Thanh toán và đặt hàng

```mermaid
sequenceDiagram
    actor Customer as Khách hàng
    participant CheckoutPage as CheckoutPage (Boundary)
    participant OrderController as OrderController (Control)
    participant OrderService as OrderService (Control)
    participant CartEntity as Cart / CartItem (Entity)
    participant OrderEntity as Order / OrderItem (Entity)
    participant DB as CSDL

    Customer->>CheckoutPage: Chọn địa chỉ và thanh toán
    CheckoutPage->>OrderController: loadCheckout()
    OrderController->>OrderService: prepareCheckoutView()
    OrderService->>CartEntity: readCurrentCart()
    CartEntity->>DB: Truy vấn cart
    DB-->>CartEntity: Cart + items
    OrderService-->>OrderController: checkoutViewData
    OrderController-->>CheckoutPage: showCheckoutConfirm()

    Customer->>CheckoutPage: clickcreateOrder()
    CheckoutPage->>OrderController: placeOrderRequest()
    OrderController->>OrderService: createOrder()
    OrderService->>OrderEntity: buildOrderAndItems()
    OrderEntity->>DB: Lưu order
    DB-->>OrderEntity: Đã lưu
    OrderService-->>OrderController: Tạo đơn addSuccess()
    OrderController-->>CheckoutPage: redirectOrderDetail()
```

## U7 - Thanh toán COD hoặc VNPAY

```mermaid
sequenceDiagram
    actor Customer as Khách hàng
    participant PaymentPage as PaymentPage (Boundary)
    participant PaymentController as PaymentController (Control)
    participant PaymentService as PaymentService (Control)
    participant PaymentEntity as Payment (Entity)
    participant VNPAY as VNPay / Mock Gateway
    participant DB as CSDL

    Customer->>PaymentPage: selectPaymentMethod()
    PaymentPage->>PaymentController: loadPaymentDetail()
    PaymentController->>PaymentService: loadPaymentByOrder(orderId)
    PaymentService->>PaymentEntity: readPayment()
    PaymentEntity->>DB: Truy vấn payment
    DB-->>PaymentEntity: Payment record
    PaymentEntity-->>PaymentService: paymentData
    PaymentService-->>PaymentPage: showPaymentStatus()

    alt COD
        Customer->>PaymentPage: chooseCOD()
        PaymentPage-->>Customer: confirmCOD()
    else VNPAY
        Customer->>PaymentPage: chooseVNPAY()
        PaymentPage->>PaymentController: createVnpayRequest()
        PaymentController->>VNPAY: buildPaymentRequest()
        alt mock-mode
            VNPAY-->>PaymentPage: redirectMockGateway()
        else sandbox thật
            VNPAY-->>PaymentPage: redirectGateway()
        end
    end
```

## U8 - Theo dõi vận chuyển

```mermaid
sequenceDiagram
    actor Customer as Khách hàng
    participant TrackingPage as TrackingPage (Boundary)
    participant ShipmentController as ShipmentController (Control)
    participant ShipmentService as ShipmentService (Control)
    participant ShipmentEntity as Shipment (Entity)
    participant GHN as GHN Sandbox / Fallback
    participant DB as CSDL

    Customer->>TrackingPage: openTrackingPage()
    TrackingPage->>ShipmentController: loadTracking()
    ShipmentController->>ShipmentService: loadTrackingByOrder(orderId)
    ShipmentService->>ShipmentEntity: loadShipment()
    ShipmentEntity->>DB: readShipment()
    DB-->>ShipmentEntity: Shipment record
    ShipmentService->>GHN: refreshFromGhn()
    alt GHN phản hồi
        GHN-->>ShipmentService: newStatus
        ShipmentService->>ShipmentEntity: updateTrackingTimeline()
        ShipmentEntity->>DB: Lưu newStatus
    else GHN lỗi / chưa cấu hình
        GHN-->>ShipmentService: null / lỗi ngoại lệ
        ShipmentService-->>ShipmentController: useInternalFallback()
    end
    ShipmentController-->>TrackingPage: Render timeline
    TrackingPage-->>Customer: showTrackingProgress()
```

## U9 - Hủy đơn khi được phép

```mermaid
sequenceDiagram
    actor Customer as Khách hàng
    participant OrderDetail as OrderDetailPage (Boundary)
    participant OrderController as OrderController (Control)
    participant OrderService as OrderService (Control)
    participant OrderEntity as Order (Entity)
    participant DB as CSDL

    Customer->>OrderDetail: clickCancelOrder()
    OrderDetail->>OrderController: cancelOrderRequest()
    OrderController->>OrderService: cancelOrderById(orderId)
    OrderService->>OrderEntity: checkOrderStatus()
    OrderEntity->>DB: Đọc order
    DB-->>OrderEntity: Order hiện tại
    alt Đơn đủ điều kiện hủy
        OrderService->>OrderEntity: setCancelled()
        OrderEntity->>DB: Lưu newStatus
        DB-->>OrderEntity: updated()
        OrderService-->>OrderController: Hủy addSuccess()
        OrderController-->>OrderDetail: showPaymentStatus() mới
    else Không được hủy
        OrderService-->>OrderController: showCancelDenied()
        OrderController-->>OrderDetail: showWarning()
    end
```

## U10 - Đánh giá sản phẩm đã mua

```mermaid
sequenceDiagram
    actor Customer as Khách hàng
    participant ProductDetail as ProductDetailPage (Boundary)
    participant ReviewController as ReviewController (Control)
    participant ReviewService as ReviewService (Control)
    participant ReviewEntity as Review (Entity)
    participant OrderEntity as Order / OrderItem (Entity)
    participant DB as CSDL

    Customer->>ProductDetail: openProductPage()
    ProductDetail->>ReviewController: loadReviewForm()
    ReviewController->>ReviewService: canReviewProduct(customerId, productId)
    ReviewService->>OrderEntity: checkPurchaseHistory()
    OrderEntity->>DB: Truy vấn đơn hàng
    DB-->>OrderEntity: emailCheckResult mua hàng
    alt Đã mua và đã giao
        ReviewService-->>ReviewController: allowReview()
        Customer->>ProductDetail: enterReview(rating, comment)
        ProductDetail->>ReviewController: submitReview()
        ReviewController->>ReviewService: createReviewRecord()
        ReviewService->>ReviewEntity: saveReview()
        ReviewEntity->>DB: Insert review
        DB-->>ReviewEntity: Đã lưu
        ReviewService-->>ProductDetail: refreshAverageRating()
    else Chưa đủ điều kiện
        ReviewService-->>ReviewController: denyReview()
        ReviewController-->>ProductDetail: hideReviewForm()
    end
```

## U11 - Cập nhật thông tin cá nhân và địa chỉ

```mermaid
sequenceDiagram
    actor Customer as Khách hàng
    participant ProfilePage as ProfilePage (Boundary)
    participant ProfileController as ProfileController (Control)
    participant ProfileService as ProfileService (Control)
    participant UserEntity as User / Address (Entity)
    participant DB as CSDL

    Customer->>ProfilePage: openProfilePage()
    ProfilePage->>ProfileController: loadProfileData()
    ProfileController->>ProfileService: loadProfileData()
    ProfileService->>UserEntity: readUserAndAddresses()
    UserEntity->>DB: queryProfileData()
    DB-->>UserEntity: Thông tin hồ sơ
    UserEntity-->>ProfileService: profileData
    ProfileService-->>ProfilePage: showProfileForm()

    Customer->>ProfilePage: editProfile()
    ProfilePage->>ProfileController: updateProfileRequest()
    ProfileController->>ProfileService: saveProfile()
    ProfileService->>UserEntity: updateProfileData()
    UserEntity->>DB: saveChanges()
    DB-->>UserEntity: updated()
    ProfileService-->>ProfilePage: Thông báo addSuccess()
```

## U12 - Tiếp nhận và xử lý đơn hàng

```mermaid
sequenceDiagram
    actor Staff as Nhân viên
    participant StaffOrderPage as StaffOrderPage (Boundary)
    participant StaffController as StaffOrderController (Control)
    participant OrderService as OrderService (Control)
    participant ShipmentService as ShipmentService (Control)
    participant OrderEntity as Order (Entity)
    participant DB as CSDL

    Staff->>StaffOrderPage: openOrderList()
    StaffOrderPage->>StaffController: loadStaffOrders()
    StaffController->>OrderService: getStaffOrders()
    OrderService->>OrderEntity: queryStaffOrders()
    OrderEntity->>DB: queryOrders()
    DB-->>OrderEntity: orderList
    OrderEntity-->>OrderService: Orders
    OrderService-->>StaffOrderPage: renderOrderList()

    Staff->>StaffOrderPage: changeOrderStatus()
    StaffOrderPage->>StaffController: submitStatusUpdate()
    StaffController->>OrderService: updateOrderStatusById()
    OrderService->>OrderEntity: setOrderStatus()
    OrderEntity->>DB: Lưu order
    DB-->>OrderEntity: Đã lưu
    OrderService->>ShipmentService: syncShipmentStatus()
```

## U13 - Quản lý sản phẩm

```mermaid
sequenceDiagram
    actor Manager as Quản lý
    participant ProductAdmin as ProductAdminPage (Boundary)
    participant AdminController as ManagerController (Control)
    participant productListervice as productListervice (Control)
    participant ProductEntity as Product / Variant (Entity)
    participant DB as CSDL

    Manager->>ProductAdmin: openProductPage()
    ProductAdmin->>AdminController: loadAdminproductList()
    AdminController->>productListervice: getproductList()
    productListervice->>ProductEntity: queryProductList()
    ProductEntity->>DB: queryproductList()
    DB-->>ProductEntity: productList
    ProductEntity-->>productListervice: Product list
    productListervice-->>ProductAdmin: renderProductList()

    alt Tạo / sửa / xóa
        Manager->>ProductAdmin: submitProductForm()
        ProductAdmin->>AdminController: POST /admin/productList/...
        AdminController->>productListervice: saveOrUpdateProductRecord()
        productListervice->>ProductEntity: writeProductData()
        ProductEntity->>DB: saveChanges()
        DB-->>ProductEntity: updated()
    end
```

## U14 - Xem báo cáo kinh doanh

```mermaid
sequenceDiagram
    actor Manager as Quản lý
    participant ReportPage as ReportPage (Boundary)
    participant ReportController as ReportController (Control)
    participant ReportService as ReportService (Control)
    participant DWH as phoneshop_dw (Entity/Data)
    participant Metabase as Metabase iframe

    Manager->>ReportPage: openReportPage()
    ReportPage->>ReportController: loadReportPage()
    ReportController->>ReportService: buildKpiSummary()
    ReportService->>DWH: readKpiSummary()
    DWH-->>ReportService: kpiSnapshot
    ReportService-->>ReportController: Report data
    alt hasEmbedUrl
        ReportController-->>Metabase: renderDashboardIframe()
        Metabase-->>ReportPage: Dashboard hiển thị
    else noEmbedUrl
        ReportController-->>ReportPage: showFallback()
    end
```

## U15 - Xử lý phản hồi của khách hàng

```mermaid
sequenceDiagram
    actor Customer as Khách hàng
    actor Staff as Nhân viên
    participant FeedbackPage as FeedbackPage (Boundary)
    participant FeedbackController as FeedbackController (Control)
    participant FeedbackService as FeedbackService (Control)
    participant FeedbackEntity as Feedback (Entity)
    participant DB as CSDL

    Customer->>FeedbackPage: submitFeedback()
    FeedbackPage->>FeedbackController: createFeedbackRequest()
    FeedbackController->>FeedbackService: createFeedbackRecord()
    FeedbackService->>FeedbackEntity: saveFeedback()
    FeedbackEntity->>DB: Insert feedback
    DB-->>FeedbackEntity: Đã lưu
    FeedbackService-->>FeedbackPage: Báo gửi addSuccess()

    Staff->>FeedbackPage: openFeedbackList()
    FeedbackPage->>FeedbackController: loadFeedbackList()
    FeedbackController->>FeedbackService: getFeedbacks()
    FeedbackService->>FeedbackEntity: queryFeedbacks()
    FeedbackEntity->>DB: Đọc phản hồi
    DB-->>FeedbackEntity: feedbackList
    FeedbackEntity-->>FeedbackService: Feedback list
    FeedbackService-->>FeedbackPage: showFeedbackProcessing()
```

## U16 - Xác thực giao dịch thanh toán

```mermaid
sequenceDiagram
    actor Gateway as Cổng thanh toán
    participant ReturnPage as PaymentReturnPage (Boundary)
    participant PaymentController as PaymentController (Control)
    participant PaymentService as PaymentService (Control)
    participant PaymentEntity as Payment (Entity)
    participant OrderEntity as Order (Entity)
    participant DB as CSDL

    Gateway->>ReturnPage: sendCallback()
    ReturnPage->>PaymentController: loadPaymentReturn()
    PaymentController->>PaymentService: verifyPaymentCallback()
    PaymentService->>PaymentEntity: readPaymentByCode()
    PaymentEntity->>DB: Truy vấn payment
    DB-->>PaymentEntity: Payment record
    PaymentService->>OrderEntity: checkOrderStatus()
    OrderEntity->>DB: Truy vấn order
    DB-->>OrderEntity: Order record
    alt Chữ ký hợp lệ và thanh toán addSuccess()
        PaymentService->>PaymentEntity: markSuccess()
        PaymentEntity->>DB: Lưu payment mới
        PaymentService->>OrderEntity: confirmOrder()
    else invalidSignatureOrFailed
        PaymentService->>PaymentEntity: markFailedOrPending()
        PaymentEntity->>DB: Lưu trạng thái
    end
    PaymentService-->>ReturnPage: Trả authenticationResult
```

## U17 - Cập nhật lộ trình vận chuyển

```mermaid
sequenceDiagram
    actor GHN as GHN / Nhân viên giao hàng
    participant TrackPage as TrackingPage (Boundary)
    participant ShipmentController as ShipmentController (Control)
    participant ShipmentService as ShipmentService (Control)
    participant ShipmentEntity as Shipment (Entity)
    participant DB as CSDL

    GHN->>ShipmentController: refreshShipmentStatus()
    ShipmentController->>ShipmentService: updateTrackingByWebhook()
    ShipmentService->>ShipmentEntity: readShipment() hiện tại
    ShipmentEntity->>DB: Truy vấn shipment
    DB-->>ShipmentEntity: Shipment record
    alt GHN có dữ liệu mới
        ShipmentService->>ShipmentEntity: updateTrackingTimeline()
        ShipmentEntity->>DB: Lưu newStatus
        DB-->>ShipmentEntity: updated()
    else GHN chưa sẵn sàng
        ShipmentService-->>ShipmentController: useInternalFallback()
    end
    ShipmentController-->>TrackPage: renderTrackingPage()
```

---

## Tự Kiểm Tra

Nếu lấy bất kỳ use case nào ở trên để so với sequence:

- Actor đã xuất hiện chưa?
- Boundary có đúng màn hình / form liên quan không?
- Control có điều phối đúng luồng không?
- Entity có được truy cập khi cần không?
- Nhánh `alt`, `opt`, `loop` có được thể hiện cho các trường hợp rẽ nhánh không?

Nếu câu trả lời là có, thì sequence diagram đã bám đúng yêu cầu của Buổi 3.

