package dev.syntax.domain.investment.repository;

import dev.syntax.domain.investment.entity.InvestPortfolioMonthlySummary;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

import java.util.Optional;

public interface InvestPortfolioMonthlySummaryRepository extends JpaRepository<InvestPortfolioMonthlySummary, Long> {
    Optional<InvestPortfolioMonthlySummary> findByCanoAndUserIdAndYearAndMonth(
            String cano, Long userId, int year, int month);

    @Query("SELECT DISTINCT new dev.syntax.domain.investment.dto.res.PortfolioDateRes(s.year, s.month) " +
           "FROM InvestPortfolioMonthlySummary s " +
           "WHERE s.cano = :cano AND s.userId = :userId " +
           "ORDER BY s.year DESC, s.month DESC")
    List<dev.syntax.domain.investment.dto.res.PortfolioDateRes> findAvailableDates(String cano, Long userId);
}
