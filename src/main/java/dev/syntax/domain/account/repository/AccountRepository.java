package dev.syntax.domain.account.repository;

import dev.syntax.domain.account.entity.Account;
import dev.syntax.domain.account.enums.AccountStatus;
import dev.syntax.domain.account.enums.AccountType;
import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface AccountRepository extends JpaRepository<Account, Long> {
    List<Account> findAllByUserId(Long userId);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("select a from Account a where a.id = :id")
    Optional<Account> findByIdWithPessimisticLock(@Param("id") Long id);

    Optional<Account> findByNumber(String number);
    List<Account> findAllByUser_IdIn(List<Long> userIds);

    Optional<Account> findFirstByUserIdAndType(Long userId, AccountType type);

    Optional<Account> findFirstByUserIdAndTypeAndStatus(Long userId, AccountType type, AccountStatus status);

    /**
     * Shadow 계좌는 직접 수정 금지(단, SOF 동기화는 예외)
     * 미러링 시에는 반드시 락으로 가져와야 동시성 충돌을 피할 수 있다.
     */
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("select a from Account a where a.user.id = :userId and a.type = :type")
    Optional<Account> findByUserIdWithPessimisticLock(
            @Param("userId") Long userId,
            @Param("type") AccountType type
    );
}
