package dev.syntax.domain.transaction.service;

import dev.syntax.domain.account.entity.Account;
import dev.syntax.domain.transaction.entity.Transaction;
import dev.syntax.domain.transaction.enums.TransactionCategory;
import dev.syntax.domain.transaction.enums.TransactionCode;
import dev.syntax.domain.transaction.enums.TransactionStatus;
import dev.syntax.domain.transaction.enums.TransactionType;
import dev.syntax.domain.transaction.repository.TransactionRepository;
import dev.syntax.domain.user.entity.CoreUser;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;


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
}
