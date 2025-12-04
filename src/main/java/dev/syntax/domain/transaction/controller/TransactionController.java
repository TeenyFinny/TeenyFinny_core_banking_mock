package dev.syntax.domain.transaction.controller;

import dev.syntax.domain.transaction.dto.TransactionAllowanceHistoryRes;
import dev.syntax.domain.transaction.dto.TransactionDetailItemRes;
import dev.syntax.domain.transaction.dto.TransactionHistoryDetailRes;
import dev.syntax.domain.transaction.dto.TransactionHistoryRes;
import dev.syntax.domain.transaction.entity.Transaction;
import dev.syntax.domain.transaction.service.TransactionService;
import dev.syntax.global.auth.annotation.CurrentUserId;
import dev.syntax.global.response.ApiResponseUtil;
import dev.syntax.global.response.BaseResponse;
import dev.syntax.global.response.SuccessCode;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDate;

/**
 * 거래 내역 조회 API 컨트롤러
 * <p>
 * Core Banking 시스템에서 특정 계좌번호에 대한 거래 내역을 조회하는 기능을 제공합니다.
 * </p>
 *
 * <ul>
 *     <li>GET /core/transaction/account/{number}</li>
 *     <li>입출금 계좌 및 목표저축 계좌 모두 조회 가능</li>
 * </ul>
 *
 * @author
 */
@Slf4j
@RestController
@RequiredArgsConstructor
@RequestMapping("/core/transaction")
public class TransactionController {

    private final TransactionService transactionService;

    /**
     * 특정 계좌번호로 거래 내역을 조회합니다.
     *
     * @param number 조회할 계좌번호 (예: "2001-002-444444")
     * @return 거래 내역 및 잔액 정보 {@link TransactionHistoryRes}
     */
    @GetMapping("/account/{number}")
    public TransactionHistoryRes getAccountTransactions(
            @CurrentUserId Long userId,
            @PathVariable String number
    ) {
        return transactionService.getHistory(userId,number);
    }

    /**
     * 특정 계좌번호로 기간별 거래 내역을 조회합니다.
     *
     * @param number 조회할 계좌번호 (예: "2001-002-444444")
     * @param startDate 조회 시작일 (예: 2025-01-01)
     * @param endDate 조회 종료일 (예: 2025-01-31)
     * @return 거래 내역 및 잔액 정보 {@link TransactionAllowanceHistoryRes}
     */
    @GetMapping("/account/{number}/period")
    public TransactionAllowanceHistoryRes getAccountTransactionsByPeriod(
            @CurrentUserId Long userId,
            @PathVariable String number,
            @RequestParam LocalDate startDate,
            @RequestParam LocalDate endDate
    ) {
        return transactionService.getHistoryByPeriod(userId,number, startDate, endDate);
    }

    /**
     * 특정 거래의 상세 정보를 조회합니다.
     * <p>
     * 거래 ID로 단일 거래를 조회하여 거래 타입, 카테고리, 승인 금액 등
     * 상세 정보를 반환합니다.
     * </p>
     *
     * @param transactionId 조회할 거래 ID
     * @return 거래 상세 정보 {@link TransactionDetailItemRes}
     */
    @GetMapping("/detail/{transactionId}")
    public TransactionDetailItemRes getTransactionDetail(
            @CurrentUserId Long userId,
            @PathVariable Long transactionId
    ) {
        return transactionService.getTransactionDetail(transactionId, userId);
    }
}
