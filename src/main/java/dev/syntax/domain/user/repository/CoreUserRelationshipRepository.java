package dev.syntax.domain.user.repository;

import dev.syntax.domain.user.entity.CoreUser;
import dev.syntax.domain.user.entity.CoreUserRelationship;
import org.springframework.data.jpa.repository.JpaRepository;

public interface CoreUserRelationshipRepository extends JpaRepository<CoreUserRelationship, Long> {

    /**
     * 부모와 자녀 간의 가족 관계가 이미 존재하는지 확인합니다.
     *
     * @param parent 부모 CoreUser
     * @param child 자녀 CoreUser
     * @return 관계가 존재하면 true, 없으면 false
     */
    boolean existsByParentAndChild(CoreUser parent, CoreUser child);
}
