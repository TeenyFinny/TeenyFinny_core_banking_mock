package dev.syntax.domain.account.entity;

import dev.syntax.domain.account.enums.AccountStatus;
import dev.syntax.domain.account.enums.AccountType;
import dev.syntax.domain.user.entity.CoreUser;
import dev.syntax.global.common.BaseTimeEntity;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;

@Entity
@Table(name = "core_account")
@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Account extends BaseTimeEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "account_id")
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private CoreUser user;

    @Column(nullable = false, length = 20)
    private String number;

    @Column(name = "product_name", length = 100, nullable = false)
    private String productName;

    @Builder.Default
    @Column(nullable = false, precision = 18, scale = 2)
    private BigDecimal balance = BigDecimal.ZERO;

    @Column(name = "interest_rate", nullable = false, precision = 5, scale = 3)
    private BigDecimal interestRate;

    @Builder.Default
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private AccountStatus status = AccountStatus.ACTIVE;

    @Builder.Default
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private AccountType type = AccountType.DEPOSIT;

    private LocalDate expiredAt;
}
