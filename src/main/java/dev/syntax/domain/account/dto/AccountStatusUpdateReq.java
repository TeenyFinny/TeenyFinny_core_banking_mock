package dev.syntax.domain.account.dto;

import dev.syntax.domain.account.enums.AccountStatus;

public record AccountStatusUpdateReq(AccountStatus status) { }
