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


    /**
     * 예수금을 증가합니다. (매도 금액 만큼 depositAmount(dnca_tot_amt) 증가)
     *
     * @param amount 매도 금액
     */
    public void deposit(Long amount) {
        this.depositAmount += amount;
        this.totalEvaluationAmount = this.depositAmount + this.securitiesEvaluationAmount;
    }

    /**
     * 예수금을 차감합니다. (매수 금액 만큼 depositAmount(dnca_tot_amt) 차감)
     *
     * @param amount 매수 금액
     */
    public void withdraw(Long amount) {
        if (this.depositAmount < amount) {
            throw new IllegalArgumentException("예수금이 부족합니다.");
        }
        this.depositAmount -= amount;
        this.totalEvaluationAmount = this.depositAmount + this.securitiesEvaluationAmount;
    }
}
