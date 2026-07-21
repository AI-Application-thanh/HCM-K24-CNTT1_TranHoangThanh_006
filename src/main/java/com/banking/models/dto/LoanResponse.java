package com.banking.models.dto;

import com.banking.models.constant.LoanStatus;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class LoanResponse {
    private Long id;
    private Long customerId;
    private String customerName;
    private BigDecimal amount;
    private LoanStatus status;
    private String rejectionReason;
    private Integer creditScore;
    private Boolean badDebtStatus;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
