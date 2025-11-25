package dev.syntax.domain.account.entity;

import dev.syntax.domain.account.enums.AccountStatus;
import dev.syntax.domain.account.enums.AccountType;
import dev.syntax.domain.transaction.entity.Transaction;
import dev.syntax.domain.user.entity.CoreUser;
import dev.syntax.global.common.BaseTimeEntity;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

/**
 * 계좌 엔티티
 * <p>
 * 은행 계좌 정보와 잔액을 관리합니다.
 * 입금/출금에 따른 잔액 변경은 incrementBalance/decrementBalance 메서드를 통해 수행됩니다.
 * </p>
 *
 * @author TeenyFinny Core Banking Team
 */
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

    @Column(nullable = false, length = 20, unique = true)
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

    @OneToMany
    private List<Transaction> transactions;

    private LocalDate expiredAt;

    /**
     * 계좌 잔액을 증가시킵니다 (입금 시 사용).
     * <p>
     * BalanceService.deposit()에서 호출됩니다.
     * </p>
     *
     * @param amount 증가할 금액 (양수)
     */
    public void incrementBalance(BigDecimal amount) {
        this.balance = this.balance.add(amount);
    }

    /**
     * 계좌 잔액을 감소시킵니다 (출금 시 사용).
     * <p>
     * BalanceService.withdraw()에서 호출됩니다.
     * </p>
     *
     * @param amount 감소할 금액 (양수)
     */
    public void decrementBalance(BigDecimal amount) {
        this.balance = this.balance.subtract(amount);
    }

}
