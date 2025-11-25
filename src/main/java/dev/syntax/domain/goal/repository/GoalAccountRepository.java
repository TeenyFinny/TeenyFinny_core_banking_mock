package dev.syntax.domain.goal.repository;

import dev.syntax.domain.goal.entity.GoalAccount;
import org.springframework.data.jpa.repository.JpaRepository;

public interface GoalAccountRepository extends JpaRepository<GoalAccount, Long> {
}
