package dev.syntax.domain.transaction.repository;

import dev.syntax.domain.transaction.entity.Transaction;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface TransactionRepository extends JpaRepository<Transaction, Long> {

    List<Transaction> findByAccount_IdOrderByTransactionDateDesc(Long AccountId);

    @Query("select a from Transaction a where a.account.number = :number")
    List<Transaction> findByNumberdOrderByTransactionDateDesc(@Param("number") String number);
}
