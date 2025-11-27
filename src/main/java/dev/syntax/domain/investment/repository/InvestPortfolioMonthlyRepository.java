package dev.syntax.domain.investment.repository;

import dev.syntax.domain.investment.entity.InvestPortfolioMonthly;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface InvestPortfolioMonthlyRepository extends JpaRepository<InvestPortfolioMonthly, Long> {
    List<InvestPortfolioMonthly> findAllByCanoAndUserIdAndYearAndMonth(
            String cano, Long userId, int year, int month);
}
