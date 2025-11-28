package dev.syntax.domain.account.dto;

public record UpdateAutoTransferDayRes(
        Long autoTransferDay,
        Integer payDay
) { }

