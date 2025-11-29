package dev.syntax.domain.transaction.dto;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import dev.syntax.domain.transaction.enums.TransactionCategory;
import dev.syntax.domain.transaction.enums.TransactionCode;

/**
 * 단일 거래 내역을 표현하는 DTO
 * <p>
 * 거래 시점, 거래 금액, 거래처, 거래 후 잔액 등
 * 하나의 거래 정보를 담아 반환합니다.
 * </p>
 *
 * @param transactionId 거래 엔티티 ID
 * @param merchantName 거래처 이름 (예: "편의점", "카페")
 * @param amount 거래 금액 (
 * @param transactionDate 거래 일시
 * @param balanceAfter 거래 후 잔액
 */
public record TransactionAllowanceItemRes(
        Long transactionId,
        String merchantName,
        BigDecimal amount,
        TransactionCode code,
        LocalDateTime transactionDate,
        TransactionCategory category,
        BigDecimal balanceAfter
) {
}
