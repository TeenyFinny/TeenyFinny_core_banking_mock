package dev.syntax.domain.transaction.service;

import dev.syntax.domain.transaction.entity.Transaction;
import dev.syntax.domain.transaction.enums.TransactionStatus;
import dev.syntax.domain.transaction.repository.TransactionRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * 관리자용 거래 서비스 구현체
 */
@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class TransactionAdminServiceImpl implements TransactionAdminService {

    private final TransactionRepository transactionRepository;

    @Override
    public Page<Transaction> getFailedTransactions(Pageable pageable) {
        return transactionRepository.findByStatus(TransactionStatus.FAIL, pageable);
    }

    @Override
    public Page<Transaction> getFailedAutoTransferTransactions(Pageable pageable) {
        return transactionRepository.findByStatusAndCodeContaining(
                TransactionStatus.FAIL,
                "AUTO",
                pageable
        );
    }
}
