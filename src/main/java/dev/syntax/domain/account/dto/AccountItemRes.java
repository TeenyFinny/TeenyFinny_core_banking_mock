package dev.syntax.domain.account.dto;

import dev.syntax.domain.account.entity.Account;
import dev.syntax.domain.account.enums.AccountType;
import lombok.Builder;

import static dev.syntax.global.service.Utils.NumberFormattingService;

/**
 * AccountItemRes
 *
 * <p>사용자 계좌의 핵심 정보를 전달하기 위한 응답 DTO입니다.</p>
 *
 * <p>Core 서비스에서 Account 엔티티를 API 응답 구조로 변환할 때 사용되며,
 * 계좌 식별자, 계좌번호, 계좌 유형, 잔액을 포함합니다.</p>
 *
 * <p>{@link #from(Account)} 정적 메서드를 통해 Account 엔티티를 안전하게
 * DTO 형태로 변환할 수 있습니다.</p>
 *
 * @param accountId     계좌 PK
 * @param accountNumber 계좌번호 (예: 1234-567-890123)
 * @param accountType   계좌 유형 (입출금, 적금 등)
 * @param balance       현재 계좌 잔액 (String 처리)
 */
@Builder
public record AccountItemRes(
        Long accountId,
        String accountNumber,
        AccountType accountType,
        String balance
) {
    /**
     * Account 엔티티를 AccountItemRes로 변환합니다.
     *
     * @param account Account 엔티티
     * @return 변환된 AccountItemRes DTO
     */
    public static AccountItemRes from(Account account) {
        String balance = NumberFormattingService(account.getBalance());
        return AccountItemRes.builder()
                .accountId(account.getId())
                .accountNumber(account.getNumber())
                .accountType(account.getType())
                .balance(balance)
                .build();
    }
}
