package dev.syntax.domain.investment.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Entity
@Table(name = "core_portfolio_monthly_summary")
@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PortfolioMonthlySummary {

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

    @Column(name = "deposit_amount", nullable = false)
    private Long depositAmount;

    @Column(name = "total_evaluation_amount", nullable = false)
    private Long totalEvaluationAmount;

    @Column(name = "total_profit_amount", nullable = false)
    private Long totalProfitAmount;

    @Column(name = "total_profit_rate", nullable = false)
    private Double totalProfitRate;

    @Column(name = "top1_name")
    private String top1Name;

    @Column(name = "top1_weight")
    private Double top1Weight;

    @Column(name = "top2_name")
    private String top2Name;

    @Column(name = "top2_weight")
    private Double top2Weight;

    @Column(name = "top3_name")
    private String top3Name;

    @Column(name = "top3_weight")
    private Double top3Weight;

    @Column(name = "etc_weight")
    private Double etcWeight;

    @Column(name = "created_at")
    private LocalDateTime createdAt;
}

