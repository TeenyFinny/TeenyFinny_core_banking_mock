package dev.syntax.domain.investment.repository;

import dev.syntax.domain.investment.entity.TradeOrder;
import org.springframework.data.jpa.repository.JpaRepository;

public interface InvestTradeOrderRepository extends JpaRepository<TradeOrder, Long> {
}
