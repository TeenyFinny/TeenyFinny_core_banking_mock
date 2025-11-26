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
public class PortfolioMonthly {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "cano")
    private String cano;

    @Column(name = "user_id")
    private Long userId;

    @Column(name = "year")
    private int year;

    @Column(name = "month")
    private int month;

    // pdno
    @Column(name = "pdno")
    private String productCode;

    // prdt_name
    @Column(name = "prdt_name")
    private String productName;

    // hldg_qty
    @Column(name = "hldg_qty")
    private Long holdingQuantity;

    // pchs_avg_pric
    @Column(name = "pchs_avg_pric")
    private Long purchaseAvgPrice;

    @Column(name = "current_price")
    private Long currentPrice;

    @Column(name = "evaluation_amount")
    private Long evaluationAmount;

    @Column(name = "profit_amount")
    private Long profitAmount;

    @Column(name = "profit_rate")
    private Double profitRate;

    @Column(name = "weight")
    private Double weight;

    @Column(name = "created_at")
    private LocalDateTime createdAt;
}
