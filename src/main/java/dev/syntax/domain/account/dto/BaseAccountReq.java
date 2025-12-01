package dev.syntax.domain.account.dto;

/**
 * 계좌 생성 요청 공통 인터페이스
 * <p>
 * 계좌 생성 요청의 공통 타입입니다.
 * </p>
 */
public interface BaseAccountReq {
    Long parentCoreId();

    Long childCoreId();
}
