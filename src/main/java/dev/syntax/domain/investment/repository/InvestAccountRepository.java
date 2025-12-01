package dev.syntax.domain.investment.repository;

import dev.syntax.domain.investment.entity.InvestAccount;
import jakarta.persistence.LockModeType;
import jakarta.persistence.QueryHint;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.QueryHints;

public interface InvestAccountRepository extends JpaRepository<InvestAccount, String> {
    /**
     * 계좌를 PESSIMISTIC_WRITE 락으로 조회
     * -> SELECT ... FOR UPDATE
     */
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @QueryHints(@QueryHint(name = "jakarta.persistence.lock.timeout", value = "5000"))
    @Override
    Optional<InvestAccount> findById(String cano);

    // [락 X] - getPortfolio 등 읽기 전용 트랜잭션에서 사용 (추가 필요)
    Optional<InvestAccount> findByCano(String cano);

    boolean existsByUserId(Long userId);
}
