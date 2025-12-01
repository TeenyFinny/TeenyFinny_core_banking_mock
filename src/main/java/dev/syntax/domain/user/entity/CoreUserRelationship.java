package dev.syntax.domain.user.entity;

import dev.syntax.global.common.BaseEntity;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "core_user_relationship")
@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CoreUserRelationship extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "relationship_id")
    private Long id;

    // 부모
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "parent_id", nullable = false)
    private CoreUser parent;

    // 자녀
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "child_id", nullable = false)
    private CoreUser child;
}
