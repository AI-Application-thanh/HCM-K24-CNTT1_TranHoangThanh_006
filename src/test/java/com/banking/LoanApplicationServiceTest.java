package com.banking;

import com.banking.exceptions.BusinessException;
import com.banking.models.constant.CustomerStatus;
import com.banking.models.constant.LoanStatus;
import com.banking.models.dto.CreateLoanRequest;
import com.banking.models.dto.LoanResponse;
import com.banking.models.entities.Customer;
import com.banking.models.entities.LoanApplication;
import com.banking.models.repositories.CustomerRepository;
import com.banking.models.repositories.LoanApplicationRepository;
import com.banking.models.services.LoanApplicationService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@Transactional
class LoanApplicationServiceTest {

    @Autowired
    private LoanApplicationService loanApplicationService;

    @Autowired
    private CustomerRepository customerRepository;

    @Autowired
    private LoanApplicationRepository loanApplicationRepository;

    private Customer sampleCustomer;

    @BeforeEach
    void setUp() {
        sampleCustomer = Customer.builder()
                .fullName("Nguyễn Văn A")
                .email("nguyenvana@gmail.com")
                .phoneNumber("0987654321")
                .identityNumber("123456789012")
                .status(CustomerStatus.ACTIVE)
                .role("CUSTOMER")
                .creditScore(700)
                .badDebtStatus(false)
                .build();
        sampleCustomer = customerRepository.save(sampleCustomer);
    }

    @Test
    @DisplayName("Test 1: Khởi tạo hồ sơ vay vốn thành công (Trạng thái PENDING)")
    void testCreateLoanApplication_Success() {
        CreateLoanRequest request = CreateLoanRequest.builder()
                .customerId(sampleCustomer.getId())
                .amount(new BigDecimal("50000000"))
                .build();

        LoanResponse response = loanApplicationService.createLoanApplication(request);

        assertNotNull(response.getId());
        assertEquals(LoanStatus.PENDING, response.getStatus());
        assertEquals(0, new BigDecimal("50000000").compareTo(response.getAmount()));
        assertNull(response.getRejectionReason());
    }

    @Test
    @DisplayName("Test 2: Duyệt khoản vay thành công cho khách hàng đủ điều kiện (Score >= 600 và không nợ xấu)")
    void testApproveLoan_Success() {
        // Given
        sampleCustomer.setCreditScore(720);
        sampleCustomer.setBadDebtStatus(false);
        customerRepository.save(sampleCustomer);

        CreateLoanRequest request = CreateLoanRequest.builder()
                .customerId(sampleCustomer.getId())
                .amount(new BigDecimal("30000000"))
                .build();
        LoanResponse loan = loanApplicationService.createLoanApplication(request);

        // When
        LoanResponse approvedLoan = loanApplicationService.approveLoanApplication(loan.getId());

        // Then
        assertEquals(LoanStatus.APPROVED, approvedLoan.getStatus());
        assertNull(approvedLoan.getRejectionReason());
    }

    @Test
    @DisplayName("Test 3: Từ chối duyệt khoản vay khi Điểm tín dụng < 600 (Ném lỗi 406 Not Acceptable)")
    void testApproveLoan_Reject_LowCreditScore() {
        // Given: Khách hàng có điểm tín dụng 550 (< 600)
        sampleCustomer.setCreditScore(550);
        sampleCustomer.setBadDebtStatus(false);
        customerRepository.save(sampleCustomer);

        CreateLoanRequest request = CreateLoanRequest.builder()
                .customerId(sampleCustomer.getId())
                .amount(new BigDecimal("20000000"))
                .build();
        LoanResponse loan = loanApplicationService.createLoanApplication(request);

        // When & Then
        BusinessException exception = assertThrows(BusinessException.class, () -> {
            loanApplicationService.approveLoanApplication(loan.getId());
        });

        // Kiểm tra mã HTTP Status Code 406
        assertEquals(406, exception.getCode());
        assertTrue(exception.getMessage().contains("Điểm tín dụng không đủ"));

        // Kiểm tra trong DB trạng thái đã được cập nhật thành REJECTED
        LoanApplication rejectedLoan = loanApplicationRepository.findById(loan.getId()).orElseThrow();
        assertEquals(LoanStatus.REJECTED, rejectedLoan.getStatus());
        assertNotNull(rejectedLoan.getRejectionReason());
    }

    @Test
    @DisplayName("Test 4: Từ chối duyệt khoản vay khi Khách hàng có nợ xấu = true (Ném lỗi 406 Not Acceptable)")
    void testApproveLoan_Reject_HasBadDebt() {
        // Given: Khách hàng có nợ xấu (badDebtStatus = true) mặc dù điểm tín dụng 750
        sampleCustomer.setCreditScore(750);
        sampleCustomer.setBadDebtStatus(true);
        customerRepository.save(sampleCustomer);

        CreateLoanRequest request = CreateLoanRequest.builder()
                .customerId(sampleCustomer.getId())
                .amount(new BigDecimal("100000000"))
                .build();
        LoanResponse loan = loanApplicationService.createLoanApplication(request);

        // When & Then
        BusinessException exception = assertThrows(BusinessException.class, () -> {
            loanApplicationService.approveLoanApplication(loan.getId());
        });

        // Kiểm tra mã HTTP Status Code 406
        assertEquals(406, exception.getCode());
        assertTrue(exception.getMessage().contains("khách hàng đang có nợ xấu"));

        // Kiểm tra trạng thái khoản vay trong DB
        LoanApplication rejectedLoan = loanApplicationRepository.findById(loan.getId()).orElseThrow();
        assertEquals(LoanStatus.REJECTED, rejectedLoan.getStatus());
        assertNotNull(rejectedLoan.getRejectionReason());
    }
}
