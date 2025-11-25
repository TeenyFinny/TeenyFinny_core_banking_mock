package dev.syntax.domain.transaction.controller;

import dev.syntax.domain.transaction.dto.TransactionHistoryRes;
import dev.syntax.domain.transaction.service.TransactionService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

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
            @PathVariable String number
    ) {
        return transactionService.getHistory(number);
    }
}
