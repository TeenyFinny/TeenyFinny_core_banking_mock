package dev.syntax.domain.transaction.service;

import dev.syntax.domain.account.entity.Account;
import dev.syntax.domain.transaction.dto.TransactionHistoryRes;
import dev.syntax.domain.transaction.enums.TransactionCategory;
import dev.syntax.domain.transaction.enums.TransactionCode;
import dev.syntax.domain.transaction.enums.TransactionStatus;
import dev.syntax.domain.transaction.enums.TransactionType;
import dev.syntax.domain.user.entity.CoreUser;

import java.math.BigDecimal;

/**
 * 거래 내역 관리 서비스
 * <p>
 * 모든 계좌 거래의 내역을 기록하고 관리합니다.
 * BalanceService와 AutoTransferService에서 호출되어 모든 잔액 변경 이력을 추적합니다.
 * </p>
 *
 * @author TeenyFinny Core Banking Team
 */
public interface TransactionService {

    /**
     * 거래 내역을 기록합니다.
     * <p>
     * 계좌의 입금/출금 및 각종 거래에 대한 내역을 Transaction 엔티티로 저장합니다.
     * </p>
     *
     * @param user         거래를 수행한 사용자
     * @param account      거래가 발생한 계좌
     * @param type         거래 타입 (카드 거래 구분, null 가능)
     * @param amount       거래 금액
     * @param balanceAfter 거래 후 잔액
     * @param merchantName 거래처명 (예: "자동이체 입금", "ATM 출금" 등)
     * @param category     거래 카테고리 (TRANSFER, SALARY, etc.)
     * @param status       거래 상태 (SUCCESS, FAIL, etc.)
     * @param code         거래 코드 (AUTO_DEPOSIT, MANUAL_WITHDRAW 등)
     */
    void record(
            CoreUser user,
            Account account,
            TransactionType type,
            BigDecimal amount,
            BigDecimal balanceAfter,
            String merchantName,
            TransactionCategory category,
            TransactionStatus status,
            TransactionCode code
    );

    /**
     * 특정 계좌의 거래내역(입금/출금/자동이체 포함)을 최신순으로 조회합니다.
     *
     * @param number 계좌번호
     * @return 잔액 + 거래내역 전체 리스트
     */
    TransactionHistoryRes getHistory(String number);
}
