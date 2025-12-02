package dev.syntax.domain.goal.dto;

import java.math.BigDecimal;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import lombok.Builder;

@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class GoalDepositEventReq {

    private String accountNo;
    private BigDecimal balanceAfter;
}
