package dev.syntax.domain.user.repository;

import dev.syntax.domain.user.entity.CoreUser;
import org.springframework.data.jpa.repository.JpaRepository;

public interface CoreUserRelationshipRepository extends JpaRepository<CoreUser, Long> {
}
