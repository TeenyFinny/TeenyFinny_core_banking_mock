package dev.syntax.domain.goal.dto;

import dev.syntax.domain.account.util.AccountNumberGenerator;
import dev.syntax.domain.goal.entity.GoalAccount;
import lombok.*;

import java.math.BigDecimal;

@Getter
@Builder
public class GoalAccountItemRes {
    private String accountNumber;
    private Long userId;
    private BigDecimal balance; // 초기 예치금 0으로 세팅

    public static GoalAccountItemRes from(GoalAccount account) {
        return GoalAccountItemRes.builder()
                .accountNumber(account.getAccountNumber())
                .userId(account.getUser().getId())
                .balance(account.getBalance())
                .accountNumber(account.getAccountNumber())
                .build();

    }
}
