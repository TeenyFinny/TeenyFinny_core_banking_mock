package dev.syntax.domain.user.dto;

/**
 * 자녀 사용자 초기화 응답 DTO
 *
 * @param coreUserId Core 사용자 ID
 */
public record ChildUserInitRes(
        Long coreUserId
) implements UserInitRes {
}
