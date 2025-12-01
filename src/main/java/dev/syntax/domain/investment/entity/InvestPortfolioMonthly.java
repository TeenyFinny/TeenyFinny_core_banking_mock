package dev.syntax.domain.investment.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Entity
@Table(name = "core_portfolio_monthly")
@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class InvestPortfolioMonthly {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "cano", nullable = false)
    private String cano;

    @Column(name = "user_id", nullable = false)
    private Long userId;

    @Column(name = "year", nullable = false)
    private int year;

    @Column(name = "month", nullable = false)
    private int month;

    // pdno
    @Column(name = "pdno", nullable = false)
    private String productCode;

    // prdt_name
    @Column(name = "prdt_name", nullable = false)
    private String productName;

    // hldg_qty
    @Column(name = "hldg_qty", nullable = false)
    private Long holdingQuantity;

    // pchs_avg_pric
    @Column(name = "pchs_avg_pric", nullable = false)
    private Long purchaseAvgPrice;

    @Column(name = "current_price", nullable = false)
    private Long currentPrice;

    @Column(name = "evaluation_amount", nullable = false)
    private Long evaluationAmount;

    @Column(name = "profit_amount", nullable = false)
    private Long profitAmount;

    @Column(name = "profit_rate", nullable = false)
    private Double profitRate;

    @Column(name = "weight", nullable = false)
    private Double weight;

    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;
}
