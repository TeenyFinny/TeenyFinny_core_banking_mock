package dev.syntax.domain.account.dto;

import com.fasterxml.jackson.annotation.JsonInclude;

import java.util.List;

/**
 * 사용자 계좌 목록 응답 DTO
 * <p>
 * 본인 계좌 목록과 자녀 계좌 목록(부모일 경우)을 포함합니다.
 * </p>
 *
 * @param accounts 본인 계좌 목록
 * @param children 자녀 계좌 목록 (자녀가 없을 경우 응답에서 제외)
 */
public record UserAccountListRes(
        List<AccountItemRes> accounts,
        @JsonInclude(JsonInclude.Include.NON_EMPTY)
        List<ChildAccountInfoRes> children
) {
}
