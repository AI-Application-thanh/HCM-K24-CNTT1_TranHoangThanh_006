package com.banking.controllers;

import com.banking.advice.ApiResponse;
import com.banking.models.dto.CreateLoanRequest;
import com.banking.models.dto.LoanResponse;
import com.banking.models.services.LoanApplicationService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/loans")
@RequiredArgsConstructor
public class LoanApplicationController {

    private final LoanApplicationService loanApplicationService;

    @PostMapping("/apply")
    public ResponseEntity<ApiResponse<LoanResponse>> applyForLoan(@Valid @RequestBody CreateLoanRequest request) {
        LoanResponse response = loanApplicationService.createLoanApplication(request);
        return new ResponseEntity<>(ApiResponse.success(response, "Khởi tạo hồ sơ vay vốn thành công"), HttpStatus.CREATED);
    }

    @PutMapping("/{id}/approve")
    public ResponseEntity<ApiResponse<LoanResponse>> approveLoan(@PathVariable("id") Long id) {
        LoanResponse response = loanApplicationService.approveLoanApplication(id);
        return ResponseEntity.ok(ApiResponse.success(response, "Phê duyệt hồ sơ vay vốn thành công"));
    }

    @GetMapping
    public ResponseEntity<ApiResponse<List<LoanResponse>>> getAllLoans() {
        List<LoanResponse> list = loanApplicationService.getAllLoanApplications();
        return ResponseEntity.ok(ApiResponse.success(list, "Lấy danh sách hồ sơ vay thành công"));
    }

    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<LoanResponse>> getLoanById(@PathVariable("id") Long id) {
        LoanResponse response = loanApplicationService.getLoanApplicationById(id);
        return ResponseEntity.ok(ApiResponse.success(response, "Lấy thông tin hồ sơ vay thành công"));
    }

    @PutMapping("/credit-info/{customerId}")
    public ResponseEntity<ApiResponse<Void>> updateCreditInfo(
            @PathVariable("customerId") Long customerId,
            @RequestParam(value = "creditScore", required = false) Integer creditScore,
            @RequestParam(value = "badDebtStatus", required = false) Boolean badDebtStatus) {
        loanApplicationService.updateCustomerCreditInfo(customerId, creditScore, badDebtStatus);
        return ResponseEntity.ok(ApiResponse.success(null, "Cập nhật thông tin tín dụng khách hàng thành công"));
    }
}
