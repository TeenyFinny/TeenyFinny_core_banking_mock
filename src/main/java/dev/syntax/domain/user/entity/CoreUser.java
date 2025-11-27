package dev.syntax.domain.user.entity;

import dev.syntax.global.common.BaseEntity;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

/**
 * 코어 사용자 엔티티
 * <p>
 * 은행 내부 시스템에서 관리하는 사용자 정보를 나타냅니다.
 * 채널 서버로부터 사용자 정보를 받아 생성되며, 계좌와 거래 내역의 기본 엔티티입니다.
 * </p>
 *
 * @author TeenyFinny Core Banking Team
 */
@Entity
@Table(name = "core_users")
@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CoreUser extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "user_id", nullable = false)
    private Long id;

    @Column(name = "name", nullable = false, length = 50)
    private String name;

    @Column(name = "phone_number", nullable = false, length = 20)
    private String phoneNumber;

    @Column(name = "birth_date", nullable = false)
    private LocalDate birthDate;

    @Column(name = "channel_user_id", nullable = false)
    private Long channelUserId;

}
