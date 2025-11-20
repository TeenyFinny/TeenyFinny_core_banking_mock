package dev.syntax.domain.investment.entity;

import dev.syntax.global.common.BaseEntity;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "core_portfolio")
@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Portfolio extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "portfolio_id")
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "cano", nullable = false)
    private InvestmentAccount cano;

    @Column(name = "user_id", nullable = false)
    private Long userId;

    @Column(name = "pdno", nullable = false, length = 12)
    private String productCode;

    @Column(name = "prdt_name", nullable = false, length = 60)
    private String productName;

    @Column(name = "hldg_qty", nullable = false)
    private Long holdingQuantity;

    @Column(name = "pchs_avg_pric", nullable = false)
    private Long purchaseAvgPrice;
}
