package dev.syntax.domain.account.entity;

import dev.syntax.domain.account.enums.AutoTransferStatus;
import dev.syntax.domain.user.entity.CoreUser;
import dev.syntax.global.common.BaseTimeEntity;
import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDate;

/**
 * 자동이체 엔티티
 * <p>
 * 매달 정해진 날짜에 자동으로 실행되는 계좌간 이체 정보를 관리합니다.
 * AutoTransferService의 execute() 메서드를 통해 실행되며,
 * 실행 후 다음 실행일(nextTransferDay)이 자동으로 갱신됩니다.
 * </p>
 *
 * @author TeenyFinny Core Banking Team
 */
@Entity
@Table(name = "core_auto_transfer")
@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AutoTransfer extends BaseTimeEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "auto_transfer_id")
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private CoreUser user;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "from_account_id", nullable = false)
    private Account fromAccount;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "to_account_id", nullable = false)
    private Account toAccount;

    @Column(nullable = false, precision = 15, scale = 2)
    private BigDecimal amount;

    @Column(length = 50)
    private String memo;

    @Column(name = "transfer_day", nullable = false)
    private Integer transferDay;

    @Setter
    @Column(name = "next_transfer_day")
    private LocalDate nextTransferDay;

    @Setter
    @Builder.Default
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private AutoTransferStatus status = AutoTransferStatus.PROCESSING;

    /**
     * 자동이체 정보를 다른 AutoTransfer 엔티티의 정보로 업데이트합니다.
     * <p>
     * 출금/입금 계좌, 금액, 메모, 이체일, 다음 실행일, 상태를 모두 업데이트합니다.
     * </p>
     *
     * @param newTransfer 새로운 정보를 담고 있는 AutoTransfer 엔티티
     */
    public void updateTransfer(BigDecimal newAmount, Integer newTransferDay, LocalDate newNextTransferDay){
        this.amount = newAmount;
        this.transferDay = newTransferDay;
        this.nextTransferDay = newNextTransferDay;
    }

    public void updateTransferDay(Integer payDay) {
        this.transferDay = payDay;
    }
}
