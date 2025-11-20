package dev.syntax.domain.user.repository;

import dev.syntax.domain.user.entity.CoreUser;
import org.springframework.data.jpa.repository.JpaRepository;

public interface CoreUserRepository extends JpaRepository<CoreUser, Long> {
}
