package dev.syntax.domain.investment.repository;

import dev.syntax.domain.investment.entity.InvestmentAccount;
import org.springframework.data.jpa.repository.JpaRepository;

public interface InvestmentAccountRepository extends JpaRepository<InvestmentAccount, String> {
}
