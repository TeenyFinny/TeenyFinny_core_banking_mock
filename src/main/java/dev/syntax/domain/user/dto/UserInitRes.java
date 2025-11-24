package dev.syntax.domain.user.dto;

/**
 * 사용자 생성 응답 공통 인터페이스
 * <p>
 * 부모와 자녀 사용자 생성 응답의 공통 타입입니다.
 * </p>
 */
public interface UserInitRes {
    /**
     * 생성된 Core 사용자 ID를 반환합니다.
     *
     * @return Core 사용자 ID
     */
    Long coreUserId();
}

