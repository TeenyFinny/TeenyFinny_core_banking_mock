package dev.syntax.domain.transaction.service;

import dev.syntax.domain.account.entity.Account;
import dev.syntax.domain.account.repository.AccountRepository;
import dev.syntax.domain.transaction.dto.TransactionHistoryRes;
import dev.syntax.domain.transaction.dto.TransactionItemRes;
import dev.syntax.domain.transaction.entity.Transaction;
import dev.syntax.domain.transaction.enums.TransactionCategory;
import dev.syntax.domain.transaction.enums.TransactionCode;
import dev.syntax.domain.transaction.enums.TransactionStatus;
import dev.syntax.domain.transaction.enums.TransactionType;
import dev.syntax.domain.transaction.repository.TransactionRepository;
import dev.syntax.domain.user.entity.CoreUser;
import dev.syntax.global.exception.BusinessException;
import dev.syntax.global.response.error.ErrorBaseCode;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;


/**
 * {@link TransactionService}의 구현체
 * <p>
 * 거래 내역을 Transaction 엔티티로 저장하는 기능을 제공합니다.
 * </p>
 *
 * @author TeenyFinny Core Banking Team
 */
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class TransactionServiceImpl implements TransactionService {

    private final TransactionRepository transactionRepository;
    private final AccountRepository accountRepository;

    /**
     * 거래 내역을 기록합니다.
     * <p>
     * Transaction 엔티티를 생성하고 현재 시간을 transactionDate로 자동 설정합니다.
     * </p>
     */
    @Transactional
    @Override
    public void record(CoreUser user,
                       Account account,
                       TransactionType type,
                       BigDecimal amount,
                       BigDecimal balanceAfter,
                       String merchantName,
                       TransactionCategory category,
                       TransactionStatus status,
                       TransactionCode code) {
        Transaction t = Transaction.builder()
                .user(user)
                .account(account)
                .code(code.name())
                .type(type != null ? type.name() : null)
                .amount(amount)
                .balanceAfter(balanceAfter)
                .merchantName(merchantName)
                .category(category.name())
                .status(status)
                .transactionDate(LocalDateTime.now())
                .build();

        transactionRepository.save(t);
    }

    /**
     * 계좌번호로 거래 내역을 조회합니다.
     * <p>
     * 계좌번호로 Account 엔티티를 조회한 후,
     * 해당 계좌의 거래 내역을 최신순으로 정렬하여 반환합니다.
     * </p>
     *
     * @param number 계좌번호
     * @return 거래 내역 리스트 및 계좌 잔액 정보 {@link TransactionHistoryRes}
     * @throws BusinessException 계좌를 찾을 수 없는 경우
     */
    @Override
    @Transactional(readOnly = true)
    public TransactionHistoryRes getHistory(String number) {

        Account account = accountRepository.findByNumber(number)
                .orElseThrow(() -> new BusinessException(ErrorBaseCode.NOT_FOUND_ENTITY));

        BigDecimal balance = account.getBalance();

        List<Transaction> transactions =
                transactionRepository.findByNumberOrderByTransactionDateDesc(number);

        List<TransactionItemRes> items = transactions.stream()
                .map(t -> new TransactionItemRes(
                        t.getId(),
                        t.getMerchantName(),
                        t.getAmount(),
                        t.getTransactionDate(),
                        t.getBalanceAfter()
                ))
                .toList();

        return new TransactionHistoryRes(items, balance);
    }
}
