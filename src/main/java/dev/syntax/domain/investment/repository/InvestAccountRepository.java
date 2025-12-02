package dev.syntax.domain.investment.repository;

import dev.syntax.domain.investment.entity.InvestAccount;
import jakarta.persistence.LockModeType;
import jakarta.persistence.QueryHint;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.QueryHints;

public interface InvestAccountRepository extends JpaRepository<InvestAccount, String> {

    Optional<InvestAccount> findByUserId(Long userId);

    // [락 X] - getPortfolio 등 읽기 전용 트랜잭션에서 사용 (추가 필요)
    Optional<InvestAccount> findByCano(String cano);

    boolean existsByUserId(Long userId);
}
