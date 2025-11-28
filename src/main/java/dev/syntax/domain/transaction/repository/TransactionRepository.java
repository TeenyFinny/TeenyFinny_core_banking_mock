package dev.syntax.domain.transaction.repository;

import dev.syntax.domain.transaction.entity.Transaction;
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
    List<Transaction> findMonthHistory(
            @Param("accountId") Long accountId,
            @Param("start") LocalDateTime start,
            @Param("end") LocalDateTime end);
}
