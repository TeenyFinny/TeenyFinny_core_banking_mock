package dev.syntax.domain.account.service;

import dev.syntax.domain.account.entity.Account;
import dev.syntax.domain.account.repository.AccountRepository;
import dev.syntax.domain.transaction.enums.TransactionCategory;
import dev.syntax.domain.transaction.enums.TransactionCode;
import dev.syntax.domain.transaction.enums.TransactionStatus;
import dev.syntax.domain.transaction.enums.TransactionType;
import dev.syntax.domain.transaction.service.TransactionService;
import dev.syntax.domain.user.entity.CoreUser;
import dev.syntax.global.exception.BusinessException;
import dev.syntax.global.response.error.ErrorBaseCode;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;

/**
 * {@link BalanceService}의 구현체
 * <p>
 * 계좌 잔액 변경과 동시에 거래 내역을 자동으로 기록하는 트랜잭션 기반 서비스입니다.
 * </p>
 *
 * @author TeenyFinny Core Banking Team
 */
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class BalanceServiceImpl implements BalanceService {

    private final AccountRepository accountRepository;
    private final TransactionService transactionService;

    /**
     * 계좌에 입금을 처리합니다.
     * <p>
     * 1. 계좌 잔액 증가 (Account.incrementBalance)
     * 2. 계좌 저장
     * 3. 거래 내역 기록 (TransactionService.record)
     * </p>
     */
    @Transactional
    @Override
    public void deposit(Account account,
                        CoreUser user,
                        BigDecimal amount,
                        String merchantName,
                        TransactionCategory category,
                        TransactionType type,
                        TransactionCode code) {

        // 잔액 증가
        account.incrementBalance(amount);
        accountRepository.save(account);

        // 거래내역 기록
        transactionService.record(
                user,
                account,
                type,
                amount,
                account.getBalance(),
                merchantName,
                category,
                TransactionStatus.SUCCESS,
                code
        );
    }

    /**
     * 계좌에서 출금을 처리합니다.
     * <p>
     * 1. 잔액 부족 검증
     * 2. 계좌 잔액 감소 (Account.decrementBalance)
     * 3. 계좌 저장
     * 4. 거래 내역 기록 (TransactionService.record)
     * </p>
     *
     * @throws BusinessException 잔액이 부족한 경우 ACCOUNT_BALANCE_NOT_ENOUGH 에러 발생
     */
    @Transactional
    @Override
    public void withdraw(Account account,
                         CoreUser user,
                         BigDecimal amount,
                         String merchantName,
                         TransactionCategory category,
                         TransactionType type,
                         TransactionCode code) {

        if (account.getBalance().compareTo(amount) < 0) {
            throw new BusinessException(ErrorBaseCode.ACCOUNT_BALANCE_NOT_ENOUGH);
        }

        // 잔액 감소
        account.decrementBalance(amount);
        accountRepository.save(account);

        // 거래내역 남기기
        transactionService.record(
                user,
                account,
                type,
                amount,
                account.getBalance(),
                merchantName,
                category,
                TransactionStatus.SUCCESS,
                code
        );
    }
}
