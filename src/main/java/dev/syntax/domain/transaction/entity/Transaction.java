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

    @Column(name = "type", length = 20, nullable = false)
    private String type;

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
