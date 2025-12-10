package dev.syntax.domain.transaction.dto;

import dev.syntax.domain.transaction.entity.Transaction;
import dev.syntax.domain.transaction.enums.TransactionCategory;
import dev.syntax.domain.transaction.enums.TransactionStatus;
import dev.syntax.domain.transaction.enums.TransactionType;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 실패한 거래 목록 조회 응답 DTO
 * <p>
 * 관리자가 실패한 거래를 조회할 때 사용됩니다.
 * </p>
 */
@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class FailedTransactionRes {

    private Long id;
    private Long userId;
    private Long accountId;
    private String accountNumber;
    private String code;
    private TransactionType type;
    private BigDecimal amount;
    private BigDecimal balanceAfter;
    private String merchantName;
    private TransactionCategory category;
    private TransactionStatus status;
    private LocalDateTime transactionDate;
    private LocalDateTime createdAt;

    public static FailedTransactionRes from(Transaction transaction) {
        return FailedTransactionRes.builder()
                .id(transaction.getId())
                .userId(transaction.getUser().getId())
                .accountId(transaction.getAccount().getId())
                .accountNumber(transaction.getAccount().getNumber())
                .code(transaction.getCode())
                .type(transaction.getType())
                .amount(transaction.getAmount())
                .balanceAfter(transaction.getBalanceAfter())
                .merchantName(transaction.getMerchantName())
                .category(transaction.getCategory())
                .status(transaction.getStatus())
                .transactionDate(transaction.getTransactionDate())
                .createdAt(transaction.getCreatedAt())
                .build();
    }
}
