package dev.syntax.domain.user.repository;

import dev.syntax.domain.user.entity.CoreUser;
import dev.syntax.domain.user.entity.CoreUserRelationship;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface CoreUserRelationshipRepository extends JpaRepository<CoreUserRelationship, Long> {

    /**
     * 부모와 자녀 간의 가족 관계가 이미 존재하는지 확인합니다.
     *
     * @param parent 부모 CoreUser
     * @param child 자녀 CoreUser
     * @return 관계가 존재하면 true, 없으면 false
     */
    boolean existsByParentAndChild(CoreUser parent, CoreUser child);

    /**
     * 부모 ID로 모든 가족 관계를 조회합니다.
     *
     * @param parentId 부모 CoreUser ID
     * @return 해당 부모의 모든 자녀 관계 목록
     */
    List<CoreUserRelationship> findAllByParent_Id(Long parentId);

    boolean existsByParent_IdAndChild_Id(Long parentId, Long childId);
}
