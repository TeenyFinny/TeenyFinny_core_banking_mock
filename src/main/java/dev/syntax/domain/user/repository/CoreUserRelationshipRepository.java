package dev.syntax.domain.user.repository;

import dev.syntax.domain.user.entity.CoreUserRelationship;
import org.springframework.data.jpa.repository.JpaRepository;

public interface CoreUserRelationshipRepository extends JpaRepository<CoreUserRelationship, Long> {
}
