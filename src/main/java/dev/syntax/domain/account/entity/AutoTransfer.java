package dev.syntax.domain.account.entity;

import dev.syntax.domain.account.enums.AutoTransferStatus;
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
    @Column(name = "user_id", nullable = false)
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

    @Column(name = "next_transfer_day")
    private LocalDate nextTransferDay;

    @Builder.Default
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private AutoTransferStatus status = AutoTransferStatus.PROCESSING;
}
