package dev.syntax.domain.account.dto;

import java.math.BigDecimal;

/**
 * 자녀 목표 자동이체 생성 요청 DTO.
 *
 * <p>
 * 부모가 자녀의 목표 계좌로 자동이체를 등록할 때 사용됩니다.
 * </p>
 *
 * @param childCoreId  자녀의 CoreUser ID
 * @param amount       자동이체 금액
 * @param transferDay  매월 실행일 (1~31)
 */
public record GoalAutoTransferCreateReq(
        Long childCoreId,
        BigDecimal amount,
        Integer transferDay
) {
}


