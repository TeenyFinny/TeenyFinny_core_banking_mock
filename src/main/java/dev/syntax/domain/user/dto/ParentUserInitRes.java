package dev.syntax.domain.user.dto;

import dev.syntax.domain.account.dto.AccountItemRes;
import dev.syntax.domain.user.entity.CoreUser;

/**
 * 부모 사용자 초기화 응답 DTO
 *
 * @param coreUserId Core 사용자 ID
 * @param account 계좌 정보
 */
public record ParentUserInitRes(
	Long coreUserId,
    AccountItemRes account
) implements UserInitRes {
    /**
     * CoreUser 엔티티와 AccountItemRes DTO로부터 ParentUserInitRes를 생성합니다.
     *
     * @param coreUser Core 사용자 엔티티
     * @param account  계좌 정보 DTO
     * @return 생성된 ParentUserInitRes DTO
     */
    public static ParentUserInitRes from(CoreUser coreUser, AccountItemRes account) {
        return new ParentUserInitRes(
                coreUser.getId(),
                account
        );
    }
}
