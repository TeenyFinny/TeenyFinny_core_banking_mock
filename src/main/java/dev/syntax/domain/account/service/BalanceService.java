package dev.syntax.domain.account.service;

import dev.syntax.domain.account.entity.Account;
import dev.syntax.domain.transaction.enums.TransactionCategory;
import dev.syntax.domain.transaction.enums.TransactionCode;
import dev.syntax.domain.transaction.enums.TransactionType;
import dev.syntax.domain.user.entity.CoreUser;
import dev.syntax.global.exception.BusinessException;

import java.math.BigDecimal;

/**
 * 계좌 잔액 관리 서비스
 * <p>
 * 계좌의 입금, 출금 처리 및 거래 내역 자동 생성을 담당합니다.
 * 모든 잔액 변경 시 TransactionService를 통해 거래 내역이 자동으로 기록됩니다.
 * </p>
 *
 * @author TeenyFinny Core Banking Team
 */
public interface BalanceService {

    /**
     * 계좌에 입금을 처리합니다.
     * <p>
     * 계좌 잔액을 증가시키고 거래 내역을 자동으로 생성합니다.
     * </p>
     *
     * @param accountId    입금할 계좌 ID
     * @param user         거래를 수행하는 사용자
     * @param amount       입금 금액 (양수)
     * @param merchantName 거래처명 (예: "자동이체 입금", "급여", "이체" 등)
     * @param category     거래 카테고리 (TRANSFER, SALARY, etc.)
     * @param type         거래 타입 (카드 거래 구분, null 가능)
     * @param code         거래 코드 (AUTO_DEPOSIT, MANUAL_DEPOSIT 등)
     */
    void deposit(Long accountId,
                 CoreUser user,
                 BigDecimal amount,
                 String merchantName,
                 TransactionCategory category,
                 TransactionType type,
                 TransactionCode code);

    /**
     * 계좌에서 출금을 처리합니다.
     * <p>
     * 계좌 잔액을 감소시키고 거래 내역을 자동으로 생성합니다.
     * 잔액이 부족한 경우 BusinessException이 발생합니다.
     * </p>
     *
     * @param accountId    출금할 계좌 ID
     * @param user         거래를 수행하는 사용자
     * @param amount       출금 금액 (양수)
     * @param merchantName 거래처명 (예: "자동이체 출금", "ATM 출금" 등)
     * @param category     거래 카테고리 (TRANSFER, WITHDRAW, etc.)
     * @param type         거래 타입 (카드 거래 구분, null 가능)
     * @param code         거래 코드 (AUTO_WITHDRAW, MANUAL_WITHDRAW 등)
     * @throws BusinessException 잔액이 부족한 경우 (ACCOUNT_BALANCE_NOT_ENOUGH)
     */
    void withdraw(Long accountId,
                  CoreUser user,
                  BigDecimal amount,
                  String merchantName,
                  TransactionCategory category,
                  TransactionType type,
                  TransactionCode code);

}
