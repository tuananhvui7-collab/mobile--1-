# Kế Hoạch Sprint 5 - 6 - 7

Tài liệu này mô tả 3 sprint tiếp theo nếu muốn đưa PhoneShop tiến gần hơn tới một hệ thống thương mại điện tử “thực sự”, ổn định hơn, có tính vận hành cao hơn và sẵn sàng mở rộng.

---

## Mục tiêu chung

- Hoàn thiện kiểm thử và độ ổn định của hệ thống.
- Làm cho tích hợp ngoài thật hơn: thanh toán, giao hàng, thông báo.
- Chuẩn bị cho triển khai thực tế: môi trường, Docker, log, backup, CI/CD.
- Cải thiện trải nghiệm người dùng và khả năng bảo trì.

---

## Sprint 5 - Ổn Định Hệ Thống Và Kiểm Thử

### Mục tiêu

Đảm bảo các luồng nghiệp vụ chính chạy ổn định, ít lỗi, có test case rõ ràng và có khả năng phát hiện lỗi sớm.

### Phạm vi

- Viết test cho các use case quan trọng:
  - đăng nhập / đăng xuất
  - tìm kiếm sản phẩm
  - thêm giỏ hàng
  - checkout
  - thanh toán
  - theo dõi giao hàng
  - hủy đơn
  - review
  - feedback
  - quản lý sản phẩm / nhân viên / đơn hàng
- Bổ sung validation cho form và API.
- Rà soát quyền truy cập theo vai trò.
- Chuẩn hóa thông báo lỗi và thông báo thành công.
- Kiểm tra các edge case:
  - hết hàng
  - giỏ rỗng
  - đơn đã hủy
  - thanh toán thất bại
  - GHN không phản hồi
  - review khi chưa mua

### Deliverables

- Bộ test case theo từng use case.
- Báo cáo các lỗi còn tồn tại.
- Danh sách rủi ro backend đã được khóa.

### Kết quả mong đợi

- Các flow chính ít gãy hơn.
- Người dùng gặp ít lỗi bất ngờ hơn.
- Dự án đủ chắc để chuyển sang tích hợp thật hơn.

---

## Sprint 6 - Tích Hợp Ngoài Thật Hơn

### Mục tiêu

Nâng cấp các tích hợp ngoài để hệ thống mang đúng cảm giác của một website thương mại điện tử có vận hành thật.

### Phạm vi

- Thanh toán:
  - hoàn thiện luồng VNPay sandbox thật nếu có credential
  - giữ mock local làm fallback dev
  - chuẩn hóa trạng thái payment và callback
- Giao hàng:
  - hoàn thiện luồng GHN sandbox thật
  - sync trạng thái vận chuyển theo webhook hoặc refresh
  - chuẩn hóa timeline và event history
- Thông báo:
  - thêm email thông báo đặt hàng
  - thêm email thông báo thanh toán / giao hàng / hủy đơn
- Audit / log:
  - thêm log nghiệp vụ cho các sự kiện quan trọng
  - giữ lịch sử thao tác ở mức đủ dùng

### Deliverables

- Luồng thanh toán thật hơn.
- Luồng vận chuyển thật hơn.
- Thông báo nghiệp vụ cơ bản.
- Log nghiệp vụ cho các trạng thái quan trọng.

### Kết quả mong đợi

- Hệ thống giống một web TMĐT thực tế hơn.
- Các luồng ngoài ít phụ thuộc vào mock hơn.
- Báo cáo, tracking và payment có tính vận hành rõ ràng hơn.

---

## Sprint 7 - Triển Khai Và Sẵn Sàng Bàn Giao

### Mục tiêu

Đưa hệ thống vào trạng thái có thể triển khai, bảo trì và bàn giao ổn định.

### Phạm vi

- Đóng gói chạy bằng Docker / Docker Compose.
- Chuẩn hóa file cấu hình cho:
  - dev
  - test
  - production
- Bổ sung backup / restore cho MySQL.
- Kiểm tra khả năng deploy web app + database + BI cùng lúc.
- Chuẩn hóa README và tài liệu bàn giao.
- Tối ưu giao diện lần cuối nếu còn điểm quá “phèn”.
- Kiểm tra lại toàn bộ luồng end-to-end.

### Deliverables

- Bộ chạy local / deploy rõ ràng.
- Tài liệu cài đặt và vận hành.
- Checklist bàn giao cuối.
- Ảnh chụp / bằng chứng test cuối.

### Kết quả mong đợi

- Có thể chạy lại dự án trên máy khác dễ hơn.
- Có thể bàn giao cho người khác tiếp tục phát triển.
- Dự án trông giống một sản phẩm hoàn chỉnh hơn.

---

## Thứ Tự Ưu Tiên Đề Xuất

1. Sprint 5: khóa lỗi và test.
2. Sprint 6: tích hợp ngoài thật hơn.
3. Sprint 7: triển khai và bàn giao.

---

## Ghi Chú Thực Tế

- Nếu thời gian ít, Sprint 5 là bắt buộc nhất.
- Nếu còn đủ thời gian, Sprint 6 giúp hệ thống “thật” hơn rõ rệt.
- Sprint 7 là sprint giúp dự án có thể bàn giao hoặc nộp final một cách đàng hoàng.

