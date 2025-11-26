package dev.syntax.domain.investment.repository;

import dev.syntax.domain.investment.entity.PortfolioMonthlySummary;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface PortfolioMonthlySummaryRepository extends JpaRepository<PortfolioMonthlySummary, Long> {
    Optional<PortfolioMonthlySummary> findByCanoAndUserIdAndYearAndMonth(
            String cano, Long userId, int year, int month);
}
