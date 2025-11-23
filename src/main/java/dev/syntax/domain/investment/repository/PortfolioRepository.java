package dev.syntax.domain.investment.repository;

import dev.syntax.domain.investment.entity.Portfolio;
import jakarta.persistence.LockModeType;
import jakarta.persistence.QueryHint;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.QueryHints;

public interface PortfolioRepository extends JpaRepository<Portfolio, Long> {

    /**
     * 특정 계좌/종목 포트폴리오를 PESSIMISTIC_WRITE 락으로 조회
     * -> 동시 매수/매도 시 수량/평단 갱신 충돌 방지
     */
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @QueryHints(@QueryHint(name = "jakarta.persistence.lock.timeout", value = "5000"))
    Optional<Portfolio> findByCano_CanoAndProductCode(String cano, String productCode);

}
