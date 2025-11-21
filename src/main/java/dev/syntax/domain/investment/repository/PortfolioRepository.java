package dev.syntax.domain.investment.repository;

import dev.syntax.domain.investment.entity.Portfolio;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface PortfolioRepository extends JpaRepository<Portfolio, Long> {
    // baas_portfolio.cano(CHAR(8)), pdno(VARCHAR(12)) 기준 조회
    Optional<Portfolio> findByCano_CanoAndProductCode(String cano, String productCode);
}
