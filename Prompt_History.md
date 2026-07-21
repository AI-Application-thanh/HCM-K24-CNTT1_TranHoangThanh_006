# LỊCH SỬ PROMPT ENGINEERING (PROMPT_HISTORY.MD)
## Dự án: Core Banking - Phân Hệ Duyệt Hồ Sơ Vay Tín Chấp (Loan Application)


## 📌 PHẦN I: KHỞI TẠO & ĐỌC HIỂU DỰ ÁN BASE CODE

### PROMPT 1: Khảo sát và Đọc hiểu Cấu trúc Base Code Dự án
* **Role (Vai trò)**: Senior Java Software Architect & Code Auditor.
* **Goal (Mục tiêu)**: Phân tích cấu trúc thư mục, các Entity cốt lõi (`Customer`, `BankAccount`), cấu hình CSDL và cơ chế bảo mật JWT hiện có của dự án Base Code.
* **Context (Bối cảnh)**: Dự án Core Banking viết bằng Java 21, Spring Boot 3.2.4, Gradle, MySQL. Mã nguồn vừa được clone về máy và cần kiểm tra tổng quan trước khi phát triển tính năng mới.
* **Constraint (Ràng buộc)**:
  - Chỉ thực hiện đọc và phân tích mã nguồn, chưa thực hiện chỉnh sửa code.
  - Tóm tắt danh sách các Entity, Controller, Repository và cấu hình database trong file `application.properties`.
* **Format (Định dạng đầu ra)**: Báo cáo phân tích tổng quan bằng tiếng Việt dưới dạng bảng và bullet points Markdown.

---

## 📌 PHẦN II: NHIỆM VỤ 1 - PHÂN TÍCH & ĐẶC TẢ YÊU CẦU (SRS)

### PROMPT 2: Soạn thảo Tài liệu Đặc tả Yêu cầu Phần mềm (`SRS.md`)
* **Role (Vai trò)**: Lead Business Analyst & Banking Solution Specialist.
* **Goal (Mục tiêu)**: Viết tài liệu đặc tả `SRS.md` tại thư mục gốc của dự án cho tính năng "Duyệt hồ sơ vay tín chấp".
* **Context (Bối cảnh)**: Giám đốc Khối Tín dụng yêu cầu xây dựng tính năng duyệt vay tín chấp. Cần mở rộng dữ liệu lưu trữ điểm tín dụng (`creditScore`), nợ xấu (`badDebtStatus`) cho khách hàng và lưu hồ sơ vay (`LoanApplication`).
* **Constraint (Ràng buộc)**:
  - Chỉ ra chi tiết các Entity hiện có (`Customer`, `BankAccount`) và Entity mới/mở rộng (`LoanApplication`, `creditScore`, `badDebtStatus`).
  - Viết thuật toán Pseudo-code kiểm tra logic duyệt vay: Tự động từ chối nếu `creditScore < 600` HOẶC `badDebtStatus == true`.
  - Định dạng chuẩn Markdown lưu tại vị trí `/SRS.md` ở thư mục gốc dự án.
* **Format (Định dạng đầu ra)**: Nội dung file Markdown (`SRS.md`) chuyên nghiệp.

---

## 📌 PHẦN III: NHIỆM VỤ 2 - LẬP TRÌNH TÍNH NĂNG BỔ SUNG

### PROMPT 3: Thiết kế các JPA Entity và Enum Mới
* **Role (Vai trò)**: Senior Database Administrator & JPA Specialist.
* **Goal (Mục tiêu)**: Tạo Enum `LoanStatus`, Entity `LoanApplication` và cập nhật Entity `Customer` bám sát tài liệu `SRS.md`.
* **Context (Bối cảnh)**: Dự án đang dùng Spring Data JPA, Lombok, MySQL.
* **Constraint (Ràng buộc)**:
  - Cập nhật `Customer.java`: Bổ sung `creditScore` (`Integer`, default 700) và `badDebtStatus` (`Boolean`, default false).
  - Tạo `LoanStatus.java`: Enum gồm `PENDING`, `APPROVED`, `REJECTED`.
  - Tạo `LoanApplication.java`: Ánh xạ quan hệ `@ManyToOne` với `Customer`, các trường `id`, `amount` (`BigDecimal`), `status`, `rejectionReason`, `createdAt`, `updatedAt`.
* **Format (Định dạng đầu ra)**: Đoạn mã Java hoàn chỉnh cho các file `Customer.java`, `LoanStatus.java`, `LoanApplication.java`.

---

### PROMPT 4: Viết Repository Interface và các DTO
* **Role (Vai trò)**: Backend Developer.
* **Goal (Mục tiêu)**: Tạo `LoanApplicationRepository` và các DTO giao tiếp API (`CreateLoanRequest`, `LoanResponse`).
* **Context (Bối cảnh)**: Các DTO nằm trong package `com.banking.models.dto`, Repository nằm trong package `com.banking.models.repositories`.
* **Constraint (Ràng buộc)**:
  - `CreateLoanRequest`: Chứa `customerId` (Not Null), `amount` (Not Null, Positive).
  - `LoanResponse`: Chứa đầy đủ thông tin khoản vay và thông tin tín dụng khách hàng.
  - `LoanApplicationRepository`: Kế thừa `JpaRepository<LoanApplication, Long>`.
* **Format (Định dạng đầu ra)**: Code Java chi tiết cho các file DTOs và Repository Interface.

---

### PROMPT 5: Xây dựng Tầng Service Xử lý Logic Kiểm tra Tín dụng (If/Else Strict Validation)
* **Role (Vai trò)**: Principal Banking Logic Developer.
* **Goal (Mục tiêu)**: Xây dựng `LoanApplicationService` với 2 chức năng chính: Tạo hồ sơ vay (`createLoanApplication`) và Duyệt hồ sơ vay (`approveLoanApplication`).
* **Context (Bối cảnh)**: Logic duyệt vay phải kiểm tra điểm tín dụng và nợ xấu của khách hàng.
* **Constraint (Ràng buộc)**:
  - Khi duyệt vay (`approveLoanApplication`):
    - Kiểm tra `creditScore < 600` HOẶC `badDebtStatus == true`.
    - Nếu vi phạm: Cập nhật trạng thái `REJECTED`, lưu lý do từ chối vào CSDL và ném ngoại lệ `BusinessException` mã lỗi 406.
    - Nếu đạt: Cập nhật trạng thái `APPROVED` và lưu CSDL.
  - Sử dụng `@Transactional` để đảm bảo tính toàn vẹn dữ liệu giao dịch ngân hàng.
* **Format (Định dạng đầu ra)**: Mã nguồn Java hoàn chỉnh cho `LoanApplicationService.java`.

---

### PROMPT 6: Xây dựng RestController Cung cấp API Tạo & Duyệt Khoản Vay
* **Role (Vai trò)**: RESTful API Specialist & Spring Security Architect.
* **Goal (Mục tiêu)**: Viết class `LoanApplicationController` trong package `com.banking.controllers`.
* **Context (Bối cảnh)**: Hệ thống cần cung cấp API RESTful cho Khách hàng nộp hồ sơ vay và Admin duyệt hồ sơ vay.
* **Constraint (Ràng buộc)**:
  - Endpoint `POST /api/v1/loans/apply`: Tiếp nhận DTO `CreateLoanRequest` (Sử dụng `@Valid`).
  - Endpoint `PUT /api/v1/loans/{id}/approve`: Duyệt hồ sơ vay theo ID.
  - Trả về đối tượng `ApiResponse<T>` đồng bộ với toàn hệ thống.
* **Format (Định dạng đầu ra)**: Mã nguồn Java chi tiết cho file `LoanApplicationController.java`.

---

## 📌 PHẦN IV: NHIỆM VỤ 3 - TỐI ƯU, XỬ LÝ NGOẠI LỆ & KIỂM THỬ DỰ ÁN

### PROMPT 7: Cập nhật Global Exception Handler Trả về HTTP Status Code 406 (Not Acceptable)
* **Role (Vai trò)**: System Reliability Engineer & Exception Handling Specialist.
* **Goal (Mục tiêu)**: Cập nhật `GlobalExceptionHandler.java` để tự động ánh xạ mã lỗi tín dụng thành HTTP Status Code **406 (Not Acceptable)**.
* **Context (Bối cảnh)**: Yêu cầu bài tập quy định khi hệ thống bắt lỗi "Điểm tín dụng không đủ" hoặc "Có nợ xấu", API bắt buộc trả về mã trạng thái HTTP 406 kèm nguyên nhân chi tiết.
* **Constraint (Ràng buộc)**:
  - Xử lý trong phương thức `@ExceptionHandler(BusinessException.class)`.
  - Sử dụng `HttpStatus.resolve(ex.getCode())` để chuyển đổi mã 406 thành `HttpStatus.NOT_ACCEPTABLE`.
  - Không phá vỡ các cấu trúc xử lý lỗi cũ (`MethodArgumentNotValidException`, `AuthenticationException`, `Exception`).
* **Format (Định dạng đầu ra)**: Đoạn mã cập nhật cho file `GlobalExceptionHandler.java`.

---

### PROMPT 8: Kiểm thử Biên dịch Dự án với Gradle & Debug Lỗi Phát sinh
* **Role (Vai trò)**: DevOps Engineer & Build QA.
* **Goal (Mục tiêu)**: Tiến hành biên dịch lại toàn bộ dự án Java bằng `./gradlew compileJava`, đảm bảo 0 lỗi syntax và dọn dẹp các lỗi lock file nếu có.
* **Context (Bối cảnh)**: Dự án đã hoàn thiện toàn bộ mã nguồn của 3 Nhiệm vụ.
* **Constraint (Ràng buộc)**:
  - Cấp quyền thực thi `chmod +x gradlew` nếu cần.
  - Xử lý triệt để các xung đột lock file trong cache Gradle (`~/.gradle/wrapper/dists/.../*.lck`).
  - Kiểm tra kết quả biên dịch phải đạt `BUILD SUCCESSFUL`.
* **Format (Định dạng đầu ra)**: Lệnh terminal thực thi và báo cáo kết quả build thành công.
