package dev.syntax.domain.account.dto;

/**
 * 자동이체 등록 요청에 대한 Core 서버 응답 DTO.
 *
 * <p>
 * Core 서버에서 자동이체를 성공적으로 생성한 후,
 * 생성된 자동이체 식별자(autoTransferId)를 채널 서버에 반환한다.
 * 채널 서버는 이 값을 primaryBankTransferId 또는 investBankTransferId
 * 필드에 저장하여 자동이체 이력과의 연결 고리를 유지한다.
 * </p>
 *
 * @param autoTransferId Core 서버 자동이체 테이블의 PK
 */
public record AutoTransferCreateRes (
    Long autoTransferId
){}
