package dev.syntax.domain.investment.repository;

import dev.syntax.domain.investment.entity.InvestPortfolioMonthlySummary;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface InvestPortfolioMonthlySummaryRepository extends JpaRepository<InvestPortfolioMonthlySummary, Long> {
    Optional<InvestPortfolioMonthlySummary> findByCanoAndUserIdAndYearAndMonth(
            String cano, Long userId, int year, int month);
}
