package dev.syntax.domain.investment.repository;

import dev.syntax.domain.investment.entity.PortfolioMonthly;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface PortfolioMonthlyRepository extends JpaRepository<PortfolioMonthly, Long> {
    List<PortfolioMonthly> findAllByCanoAndUserIdAndYearAndMonth(
            String cano, Long userId, int year, int month);
}
