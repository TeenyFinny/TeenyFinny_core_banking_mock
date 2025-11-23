package dev.syntax.domain.transaction.entity;

import dev.syntax.domain.account.entity.Account;
import dev.syntax.domain.transaction.enums.TransactionStatus;
import dev.syntax.domain.user.entity.CoreUser;
import dev.syntax.global.common.BaseTimeEntity;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 거래 내역 엔티티
 * <p>
 * 모든 계좌 거래(입금, 출금, 자동이체 등)의 내역을 기록합니다.
 * TransactionService.record()를 통해 생성되며,
 * BalanceService의 모든 잔액 변경 발생 시 자동으로 기록됩니다.
 * </p>
 *
 * @author TeenyFinny Core Banking Team
 */
@Entity
@Table(name = "core_transaction")
@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Transaction extends BaseTimeEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "transaction_id")
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private CoreUser user;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "account_id", nullable = false)
    private Account account;

    @Column(name = "code", length = 10, nullable = false)
    private String code;

    @Column(name = "type", length = 20, nullable = true)
    private String type;

    @Column(name = "amount", nullable = false)
    private BigDecimal amount;

    @Column(name = "balance_after", nullable = false, precision = 15, scale = 3)
    private BigDecimal balanceAfter;

    @Column(name = "merchant_name", length = 50, nullable = false)
    private String merchantName;

    @Column(name = "category", length = 30, nullable = false)
    private String category;

    @Builder.Default
    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false)
    private TransactionStatus status = TransactionStatus.PENDING;

    @Column(name = "transaction_date", nullable = false)
    private LocalDateTime transactionDate;
}
