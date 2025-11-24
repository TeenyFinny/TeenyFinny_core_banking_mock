package dev.syntax.domain.investment.repository;

import dev.syntax.domain.investment.entity.InvestmentAccount;
import jakarta.persistence.LockModeType;
import jakarta.persistence.QueryHint;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.QueryHints;

public interface InvestmentAccountRepository extends JpaRepository<InvestmentAccount, String> {
    /**
     * 계좌를 PESSIMISTIC_WRITE 락으로 조회
     * -> SELECT ... FOR UPDATE
     */
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @QueryHints(@QueryHint(name = "jakarta.persistence.lock.timeout", value = "5000"))
    @Override
    Optional<InvestmentAccount> findById(String cano);
}
