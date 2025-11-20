package dev.syntax.domain.investment.entity;

import dev.syntax.domain.investment.enums.OrderStatus;
import dev.syntax.domain.investment.enums.TradeType;
import dev.syntax.global.common.BaseEntity;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Entity
@Table(name = "core_trade_orders")
@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class TradeOrder extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "order_id")
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "cano", nullable = false)
    private InvestmentAccount cano;

    @Column(name = "user_id", nullable = false)
    private Long userId;

    @Column(name = "gt_uid", length = 32)
    private String globalUid;

    @Column(name = "ord_tmd", nullable = false)
    private LocalDateTime orderTime;

    @Enumerated(EnumType.STRING)
    @Column(name = "tr_id", nullable = false)
    private TradeType tradeType;

    @Column(name = "pdno", length = 12, nullable = false)
    private String productCode;

    @Column(name = "prdt_name", length = 60, nullable = false)
    private String productName;

    @Column(name = "ord_qty", nullable = false)
    private Integer quantity;

    @Column(name = "ord_unpr", nullable = false)
    private Long price;

    @Column(name = "excg_id_dvsn_cd", length = 3, nullable = false)
    private String exchangeDivisionCode;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false)
    @Builder.Default
    private OrderStatus status = OrderStatus.REQUESTED;
}
