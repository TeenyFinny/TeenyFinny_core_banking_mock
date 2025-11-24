package dev.syntax.domain.account.service;

import dev.syntax.domain.account.dto.AccountItemRes;
import dev.syntax.domain.account.entity.Account;
import dev.syntax.domain.user.entity.CoreUser;

import java.util.List;

/**
 * AccountService
 *
 * <p>Core 서버에서 사용자(CoreUser)의 계좌 정보를 조회하는 서비스 인터페이스입니다.</p>
 *
 * <p>channel 서버로부터 전달받은 coreUserId를 기반으로,
 * 해당 사용자에게 속한 모든 계좌 목록을 반환합니다.</p>
 *
 * <p>조회된 계좌는 {@link AccountItemRes} DTO 형태로 변환되어 제공됩니다.</p>
 */
public interface AccountService {
    
    /**
     * 주어진 coreUserId에 해당하는 사용자의 모든 계좌를 조회합니다.
     *
     * @param coreUserId Core 서버 내부 사용자의 고유 식별자
     * @return 사용자 계좌 목록 (없을 경우 빈 리스트)
     */
    List<AccountItemRes> getUserAccounts(Long coreUserId);

    /**
     * 입출금 통장 계좌를 생성합니다.
     *
     * @param user 계좌 소유자
     * @return 생성된 계좌
     */
    Account createDepositAccount(CoreUser user);
}
