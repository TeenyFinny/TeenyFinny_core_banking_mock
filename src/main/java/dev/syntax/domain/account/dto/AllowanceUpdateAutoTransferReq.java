package dev.syntax.domain.account.dto;

import java.math.BigDecimal;

public record AllowanceUpdateAutoTransferReq (
    BigDecimal amount,
    Integer transferDay
){}