package dev.syntax.domain.investment.entity;

import dev.syntax.global.common.BaseEntity;
import dev.syntax.global.exception.BusinessException;
import dev.syntax.global.response.error.ErrorInvestmentCode;
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

    @Column(name = "dnca_tot_amt", nullable = false)
    private Long depositAmount;



    /**
     * 예수금을 증가합니다. (매도 금액 만큼 depositAmount(dnca_tot_amt) 증가)
     *
     * @param amount 매도 금액
     */
    public void deposit(Long amount) {
        this.depositAmount += amount;
    }

    /**
     * 예수금을 차감합니다. (매수 금액 만큼 depositAmount(dnca_tot_amt) 차감)
     *
     * @param amount 매수 금액
     */
    public void withdraw(Long amount) {
        if (this.depositAmount < amount) {
            throw new BusinessException(ErrorInvestmentCode.INSUFFICIENT_BALANCE);
        }
        this.depositAmount -= amount;
    }
}
