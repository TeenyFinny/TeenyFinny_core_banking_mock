package dev.syntax.domain.account.dto;

import dev.syntax.domain.account.enums.AccountStatus;

public record AccountStatusUpdateRes(
        Long accountId,
        AccountStatus status
) {
}
