# Software Requirements Specification (SRS)
## Hệ Thống Core Banking - Phân Hệ Duyệt Hồ Sơ Vay Tín Chấp (Loan Application)

---

### 1. TỔNG QUAN HỆ THỐNG & MỤC TIÊU DỰ ÁN
* **Tên hệ thống**: Core Banking System
* **Phân hệ**: Quản lý & Duyệt Hồ Sơ Vay Tín Chấp (Unsecured Loan Application)
* **Phiên bản**: 1.0.0
* **Công nghệ nền tảng**: Java 21, Spring Boot 3.2.4, Spring Data JPA, Spring Security (JWT), MySQL.

---

### 2. THỰC THỂ DỮ LIỆU HIỆN CÓ (EXISTING ENTITIES)

#### 2.1. Entity `Customer` (Bảng `customers`)
Lưu trữ thông tin cá nhân và tài khoản truy cập của khách hàng.

| Tên thuộc tính | Kiểu dữ liệu | Ràng buộc | Mô tả |
| :--- | :--- | :--- | :--- |
| `id` | `Long` | Primary Key, Auto Increment | Định danh khách hàng |
| `fullName` | `String` | Not Null, Length(100) | Họ và tên khách hàng |
| `email` | `String` | Unique, Length(100) | Địa chỉ Email |
| `password` | `String` | Length(100) | Mật khẩu (đã mã hóa) |
| `phoneNumber` | `String` | Unique, Not Null, Length(20) | Số điện thoại |
| `identityNumber` | `String` | Unique, Not Null, Length(20) | Số CMND/CCCD |
| `dateOfBirth` | `LocalDate` | Nullable | Ngày tháng năm sinh |
| `address` | `String` | Length(255) | Địa chỉ thường trú |
| `status` | `CustomerStatus` | Enum (ACTIVE, INACTIVE, LOCKED) | Trạng thái tài khoản |
| `role` | `String` | Nullable | Vai trò (`CUSTOMER`, `ADMIN`) |

#### 2.2. Entity `BankAccount` (Bảng `bank_accounts`)
Lưu trữ thông tin tài khoản thanh toán / tiền gửi của khách hàng.

| Tên thuộc tính | Kiểu dữ liệu | Ràng buộc | Mô tả |
| :--- | :--- | :--- | :--- |
| `id` | `Long` | Primary Key, Auto Increment | Định danh tài khoản |
| `accountNumber` | `String` | Unique, Not Null, Length(50) | Số tài khoản ngân hàng |
| `balance` | `BigDecimal` | Not Null, Precision(19,4) | Số dư hiện tại (mặc định: 0) |
| `currency` | `String` | Not Null, Length(10) | Loại tiền tệ (mặc định: "VND") |
| `accountType` | `AccountType` | Enum (CHECKING, SAVINGS, CREDIT) | Loại tài khoản |
| `status` | `AccountStatus` | Enum (ACTIVE, LOCKED, CLOSED) | Trạng thái tài khoản |
| `createdAt` | `LocalDateTime` | Updatable = false | Thời gian tạo tài khoản |
| `customer` | `Customer` | ManyToOne, Not Null | Thông tin khách hàng sở hữu |

---

### 3. THIẾT KẾ CẤU TRÚC DỮ LIỆU MỚI (NEW DATA STRUCTURES)

#### 3.1. Bổ sung Thông tin Điểm tín dụng & Nợ xấu cho `Customer` / `CreditHistory`
Để phục vụ việc kiểm tra điều kiện vay, hệ thống lưu trữ các thuộc tính tín dụng của khách hàng:

| Tên thuộc tính | Kiểu dữ liệu | Ràng buộc | Mô tả |
| :--- | :--- | :--- | :--- |
| `creditScore` | `Integer` | Range (300 - 850) | Điểm tín dụng cá nhân của khách hàng |
| `badDebtStatus` | `Boolean` | Default: `false` | Trạng thái nợ xấu (`true`: Có nợ xấu, `false`: Không có nợ xấu) |

#### 3.2. Entity Mới `LoanApplication` (Bảng `loan_applications`)
Lưu trữ các hồ sơ yêu cầu vay tín chấp do khách hàng khởi tạo.

| Tên thuộc tính | Kiểu dữ liệu | Ràng buộc | Mô tả |
| :--- | :--- | :--- | :--- |
| `id` | `Long` | Primary Key, Auto Increment | Mã hồ sơ vay |
| `customer` | `Customer` | ManyToOne, Not Null | Khách hàng nộp hồ sơ vay |
| `amount` | `BigDecimal` | Not Null, Precision(19,4) | Số tiền yêu cầu vay |
| `status` | `LoanStatus` | Enum (PENDING, APPROVED, REJECTED) | Trạng thái hồ sơ vay |
| `rejectionReason` | `String` | Nullable, Length(555) | Lý do từ chối nếu không đạt điều kiện |
| `createdAt` | `LocalDateTime` | Updatable = false | Thời gian gửi yêu cầu vay |
| `updatedAt` | `LocalDateTime` | Nullable | Thời gian duyệt/xử lý hồ sơ |

---

### 4. ĐẶC TẢ YÊU CẦU CHỨC NĂNG (FUNCTIONAL REQUIREMENTS)

#### FR-01: Khởi tạo Yêu cầu Vay vốn (Submit Loan Application)
* **Actor**: Customer (Khách hàng)
* **Input**: Số tiền vay (`amount`).
* **Processing**:
  1. Xác thực tài khoản khách hàng qua JWT Token.
  2. Tạo mới đối tượng `LoanApplication` với thông tin khách hàng và số tiền vay.
  3. Đặt trạng thái hồ sơ ban đầu là `PENDING`.
* **Output**: Thông tin hồ sơ vay vừa tạo (ID, Amount, Status = `PENDING`, CreatedAt).

#### FR-02: Quản lý & Cập nhật Điểm Tín Dụng (Credit Score Management)
* **Actor**: Admin / System
* **Processing**: Lưu trữ và cập nhật điểm tín dụng (`creditScore`) và trạng thái nợ xấu (`badDebtStatus`) cho từng `Customer`.

#### FR-03: Duyệt Hồ Sơ Vay Tín Chấp (Approve Loan Application)
* **Actor**: Admin / Nhân viên tín dụng
* **Input**: Mã hồ sơ vay (`loanId`).
* **Rules & Validation Logic**:
  * Kiểm tra hồ sơ tồn tại và ở trạng thái `PENDING`.
  * Lấy thông tin `creditScore` và `badDebtStatus` của khách hàng sở hữu hồ sơ.
  * ❌ **Điều kiện TỰ ĐỘNG TỪ CHỐI (AUTO-REJECT)**:
    - Nếu `creditScore < 600`
    - HOẶC `badDebtStatus == true`
    - **Hành động**: Chuyển trạng thái khoản vay thành `REJECTED`, ghi rõ lý do từ chối, lưu DB và ném ra ngoại lệ `BusinessException` thông báo từ chối khoản vay.
  * ✅ **Điều kiện THỦY CHẤP / PHÊ DUYỆT (APPROVE)**:
    - Khi `creditScore >= 600` VÀ `badDebtStatus == false`.
    - **Hành động**: Chuyển trạng thái khoản vay thành `APPROVED`, lưu DB và trả về phản hồi phê duyệt thành công.

---

### 5. THUẬT TOÁN KIỂM TRA ĐIỀU KIỆN DUYỆT VAY (PSEUDO-CODE)

```text
ALGORITHM ApproveLoanApplication(loanId, adminId):
    INPUT: 
        loanId: Long (Mã hồ sơ vay)
        adminId: Long (Mã nhân viên thực hiện duyệt)
    OUTPUT: 
        LoanApplicationUpdated hoặc Exception Lỗi Nghiệp Vụ

    1. Lấy thông tin hồ sơ vay từ DB:
       loanApp = loanApplicationRepository.findById(loanId)
       IF loanApp IS NULL THEN
           THROW NotFoundException("Hồ sơ vay không tồn tại trên hệ thống")
       END IF

    2. Kiểm tra trạng thái hiện tại của hồ sơ:
       IF loanApp.status != LoanStatus.PENDING THEN
           THROW BusinessException("Hồ sơ vay không ở trạng thái chờ duyệt (PENDING)")
       END IF

    3. Lấy thông tin khách hàng liên kết với hồ sơ:
       customer = loanApp.getCustomer()
       creditScore = customer.getCreditScore()
       badDebtStatus = customer.getBadDebtStatus()

    4. Xử lý thuật toán kiểm tra điều kiện tín dụng:
       IF creditScore < 600 OR badDebtStatus == TRUE THEN
           // Cập nhật trạng thái từ chối hồ sơ
           loanApp.setStatus(LoanStatus.REJECTED)
           loanApp.setRejectionReason("Hồ sơ bị từ chối do Điểm tín dụng không đạt (< 600) hoặc Khách hàng đang có nợ xấu.")
           loanApp.setUpdatedAt(CurrentDateTime())
           
           // Lưu vào CSDL lịch sử từ chối
           loanApplicationRepository.save(loanApp)
           
           // Ném ngoại lệ chặn giao dịch và báo lỗi về Client
           THROW BusinessException("RỦI RO TÍN DỤNG: Hồ sơ vay bị từ chối. Lý do: Điểm tín dụng dưới 600 hoặc đang có nợ xấu.")
       ELSE
           // Đủ điều kiện phê duyệt
           loanApp.setStatus(LoanStatus.APPROVED)
           loanApp.setRejectionReason(NULL)
           loanApp.setUpdatedAt(CurrentDateTime())
           
           // Lưu trạng thái đã phê duyệt vào CSDL
           savedLoan = loanApplicationRepository.save(loanApp)
           
           RETURN savedLoan
       END IF
END ALGORITHM
```

---

### 6. YÊU CẦU PHI CHỨC NĂNG (NON-FUNCTIONAL REQUIREMENTS)
1. **Tính Toàn vẹn Dữ liệu (Data Integrity)**: Sử dụng `@Transactional` cho API duyệt vay nhằm đảm bảo trạng thái hồ sơ được cập nhật đồng bộ.
2. **Tính Bảo mật (Security & Authorization)**: Chỉ người dùng có vai trò `ADMIN` mới có quyền gọi API duyệt hồ sơ.
3. **Độ chính xác Tiền tệ (Financial Accuracy)**: Số tiền vay `amount` sử dụng kiểu dữ liệu `BigDecimal` với scale=4 để tránh sai số dấu chân vịt (floating-point precision error).
