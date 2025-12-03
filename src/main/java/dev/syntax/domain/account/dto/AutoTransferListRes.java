package dev.syntax.domain.account.dto;

import dev.syntax.domain.account.entity.AutoTransfer;
import dev.syntax.domain.account.enums.AutoTransferStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * 자동이체 목록 조회 응답 DTO
 * <p>
 * 관리자가 전체 자동이체 목록을 조회할 때 사용됩니다.
 * </p>
 */
@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AutoTransferListRes {

    private Long id;
    private Long userId;
    private Long fromAccountId;
    private Long toAccountId;
    private BigDecimal amount;
    private String memo;
    private Integer transferDay;
    private LocalDate nextTransferDay;
    private AutoTransferStatus status;
    private LocalDateTime createdAt;

    public static AutoTransferListRes from(AutoTransfer autoTransfer) {
        return AutoTransferListRes.builder()
                .id(autoTransfer.getId())
                .userId(autoTransfer.getUser().getId())
                .fromAccountId(autoTransfer.getFromAccount().getId())
                .toAccountId(autoTransfer.getToAccount().getId())
                .amount(autoTransfer.getAmount())
                .memo(autoTransfer.getMemo())
                .transferDay(autoTransfer.getTransferDay())
                .nextTransferDay(autoTransfer.getNextTransferDay())
                .status(autoTransfer.getStatus())
                .createdAt(autoTransfer.getCreatedAt())
                .build();
    }
}
