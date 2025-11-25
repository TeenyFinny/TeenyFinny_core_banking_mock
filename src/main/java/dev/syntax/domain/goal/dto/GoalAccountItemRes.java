package dev.syntax.domain.goal.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class GoalAccountItemRes {
    private String accountNumber;
    private Long userId;
    private BigDecimal balance; // 초기 예치금 0으로 세팅
}
