package dev.syntax.domain.investment.repository;

import dev.syntax.domain.investment.entity.Portfolio;
import org.springframework.data.jpa.repository.JpaRepository;

public interface PortfolioRepository extends JpaRepository<Portfolio, Long> {
}
