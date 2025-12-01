package dev.syntax.domain.account.dto;

import java.math.BigDecimal;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

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
	@NotNull(message = "자녀 ID는 필수입니다.")
	Long childCoreId,
	@NotNull(message = "금액은 필수입니다.")
	@Positive(message = "금액은 0보다 커야 합니다.")
	BigDecimal amount,
	@NotNull(message = "이체일은 필수입니다.")
	@Min(value = 1, message = "이체일은 1일 이상이어야 합니다.")
	@Max(value = 31, message = "이체일은 31일 이하여야 합니다.")
	Integer transferDay
) {
}
