package dev.syntax.domain.transaction.service;

import dev.syntax.domain.transaction.entity.Transaction;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

/**
 * 관리자용 거래 서비스 인터페이스
 * <p>
 * 관리자 전용 거래 조회 기능을 제공합니다.
 * 기존 TransactionService와 분리하여 관리자 기능의 독립성을 보장합니다.
 * </p>
 */
public interface TransactionAdminService {

    /**
     * 실패한 거래 조회 (페이징)
     */
    Page<Transaction> getFailedTransactions(Pageable pageable);

    /**
     * 자동이체 관련 실패 거래 조회 (페이징)
     */
    Page<Transaction> getFailedAutoTransferTransactions(Pageable pageable);
}
