package dev.syntax.domain.account.dto;

import dev.syntax.domain.account.enums.AccountType;

public record DepositAccountReq(
        Long parentCoreId,
        Long childCoreId,
        AccountType accountType
) implements BaseAccountReq {
}
