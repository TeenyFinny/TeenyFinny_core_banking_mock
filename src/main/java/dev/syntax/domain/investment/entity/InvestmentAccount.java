package dev.syntax.domain.investment.entity;

import dev.syntax.global.common.BaseEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "core_investment_account")
@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class InvestmentAccount extends BaseEntity {

    @Id
    @Column(name = "cano", length = 8, nullable = false)
    private String cano;

    @Column(name = "user_id", nullable = false)
    private Long userId;

    @Column(name = "tot_evlu_amt", nullable = false)
    private Long totalEvaluationAmount;

    @Column(name = "dnca_tot_amt", nullable = false)
    private Long depositAmount;

    @Column(name = "scts_evlu_amt", nullable = false)
    private Long securitiesEvaluationAmount;
}
