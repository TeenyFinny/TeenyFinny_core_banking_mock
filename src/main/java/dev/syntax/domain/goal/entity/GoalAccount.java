package dev.syntax.domain.goal.entity;


import dev.syntax.domain.user.entity.CoreUser;
import dev.syntax.global.common.BaseEntity;
import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.util.UUID;

@Entity
@Table(name = "goal_account")
@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class GoalAccount extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private CoreUser user;

    @Column(name = "goal_name", nullable = false)
    private String goalName;

    @Column(name = "account_number", nullable = false, unique = true)
    private String accountNumber;

    @Column(name = "balance", nullable = false)
    private BigDecimal balance;
}
