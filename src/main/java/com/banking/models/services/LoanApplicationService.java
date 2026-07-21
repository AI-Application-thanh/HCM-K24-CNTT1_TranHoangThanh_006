package com.banking.models.services;

import com.banking.exceptions.BusinessException;
import com.banking.models.constant.LoanStatus;
import com.banking.models.dto.CreateLoanRequest;
import com.banking.models.dto.LoanResponse;
import com.banking.models.entities.Customer;
import com.banking.models.entities.LoanApplication;
import com.banking.models.repositories.CustomerRepository;
import com.banking.models.repositories.LoanApplicationRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class LoanApplicationService {

    private final LoanApplicationRepository loanApplicationRepository;
    private final CustomerRepository customerRepository;

    @Transactional
    public LoanResponse createLoanApplication(CreateLoanRequest request) {
        Customer customer = customerRepository.findById(request.getCustomerId())
                .orElseThrow(() -> new BusinessException(404, "Khách hàng không tồn tại trên hệ thống"));

        LoanApplication loanApplication = LoanApplication.builder()
                .customer(customer)
                .amount(request.getAmount())
                .status(LoanStatus.PENDING)
                .build();

        LoanApplication saved = loanApplicationRepository.save(loanApplication);
        return mapToResponse(saved);
    }

    @Transactional
    public LoanResponse approveLoanApplication(Long loanId) {
        LoanApplication loanApp = loanApplicationRepository.findById(loanId)
                .orElseThrow(() -> new BusinessException(404, "Hồ sơ vay không tồn tại trên hệ thống"));

        if (loanApp.getStatus() != LoanStatus.PENDING) {
            throw new BusinessException(400, "Hồ sơ vay không ở trạng thái chờ duyệt (PENDING)");
        }

        Customer customer = loanApp.getCustomer();
        Integer creditScore = customer.getCreditScore() != null ? customer.getCreditScore() : 700;
        Boolean badDebtStatus = customer.getBadDebtStatus() != null ? customer.getBadDebtStatus() : false;

        // Logic kiểm tra tín dụng nghiêm ngặt
        if (creditScore < 600 || Boolean.TRUE.equals(badDebtStatus)) {
            String reason = String.format("Từ chối: Điểm tín dụng không đủ (%d < 600) hoặc khách hàng đang có nợ xấu (%s)",
                    creditScore, badDebtStatus ? "Có nợ xấu" : "Không có nợ xấu");
            
            loanApp.setStatus(LoanStatus.REJECTED);
            loanApp.setRejectionReason(reason);
            loanApplicationRepository.save(loanApp);

            // Ném ngoại lệ với mã lỗi 406 (Not Acceptable) theo đúng yêu cầu bài tập
            throw new BusinessException(406, "Hồ sơ vay bị từ chối: " + reason);
        }

        loanApp.setStatus(LoanStatus.APPROVED);
        loanApp.setRejectionReason(null);
        LoanApplication approved = loanApplicationRepository.save(loanApp);
        return mapToResponse(approved);
    }

    @Transactional(readOnly = true)
    public List<LoanResponse> getAllLoanApplications() {
        return loanApplicationRepository.findAll().stream()
                .map(this::mapToResponse)
                .toList();
    }

    @Transactional(readOnly = true)
    public LoanResponse getLoanApplicationById(Long id) {
        LoanApplication loanApp = loanApplicationRepository.findById(id)
                .orElseThrow(() -> new BusinessException(404, "Hồ sơ vay không tồn tại"));
        return mapToResponse(loanApp);
    }

    @Transactional
    public void updateCustomerCreditInfo(Long customerId, Integer creditScore, Boolean badDebtStatus) {
        Customer customer = customerRepository.findById(customerId)
                .orElseThrow(() -> new BusinessException(404, "Khách hàng không tồn tại"));
        if (creditScore != null) {
            customer.setCreditScore(creditScore);
        }
        if (badDebtStatus != null) {
            customer.setBadDebtStatus(badDebtStatus);
        }
        customerRepository.save(customer);
    }

    private LoanResponse mapToResponse(LoanApplication loanApp) {
        Customer c = loanApp.getCustomer();
        return LoanResponse.builder()
                .id(loanApp.getId())
                .customerId(c.getId())
                .customerName(c.getFullName())
                .amount(loanApp.getAmount())
                .status(loanApp.getStatus())
                .rejectionReason(loanApp.getRejectionReason())
                .creditScore(c.getCreditScore())
                .badDebtStatus(c.getBadDebtStatus())
                .createdAt(loanApp.getCreatedAt())
                .updatedAt(loanApp.getUpdatedAt())
                .build();
    }
}
