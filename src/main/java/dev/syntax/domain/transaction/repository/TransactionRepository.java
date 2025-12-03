package dev.syntax.domain.transaction.repository;

import dev.syntax.domain.transaction.entity.Transaction;
import dev.syntax.domain.transaction.enums.TransactionStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;
import java.util.List;

public interface TransactionRepository extends JpaRepository<Transaction, Long> {

    List<Transaction> findByAccount_IdOrderByTransactionDateDesc(Long accountId);

    @Query("select a from Transaction a where a.account.number = :number order by a.transactionDate desc")
    List<Transaction> findByNumberOrderByTransactionDateDesc(@Param("number") String number);

    @Query("SELECT t FROM Transaction t " +
       "WHERE t.account.id = :accountId " +
       "AND t.transactionDate >= :start " +
       "AND t.transactionDate < :end " +
       "ORDER BY t.transactionDate DESC")
    List<Transaction> findHistoryByPeriod(
            @Param("accountId") Long accountId,
            @Param("start") LocalDateTime start,
            @Param("end") LocalDateTime end);

    /**
     * 실패한 거래 조회 (페이징)
     */
    Page<Transaction> findByStatus(TransactionStatus status, Pageable pageable);

    /**
     * 자동이체 관련 실패 거래 조회 (페이징)
     * code에 "AUTO"가 포함된 거래만 조회
     */
    @Query("SELECT t FROM Transaction t WHERE t.status = :status AND t.code LIKE %:codePattern%")
    Page<Transaction> findByStatusAndCodeContaining(
            @Param("status") TransactionStatus status,
            @Param("codePattern") String codePattern,
            Pageable pageable
    );
}
