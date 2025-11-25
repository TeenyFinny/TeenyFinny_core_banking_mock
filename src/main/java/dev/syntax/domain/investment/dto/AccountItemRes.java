package dev.syntax.domain.investment.dto;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class AccountItemRes {
    private String accountNumber;
    private Long userId;
    private Long balance;
}
