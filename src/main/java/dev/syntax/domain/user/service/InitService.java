package dev.syntax.domain.user.service;

import dev.syntax.domain.user.dto.ChannelUserInitReq;
import dev.syntax.domain.user.dto.UserInitRes;
import dev.syntax.global.exception.BusinessException;

/**
 * 사용자 생성 서비스
 * <p>
 * 사용자 가입 시 필요한 초기화 작업을 처리합니다.
 * </p>
 */
public interface InitService {

    /**
     * 사용자 생성을 처리합니다.
     * <p>
     * 요청의 Role에 따라 부모와 자녀를 구분하여 처리합니다.
     * </p>
     * <ul>
     *   <li>PARENT: CoreUser 생성, 계좌 생성, 초기 잔액 100만원 입금</li>
     *   <li>CHILD: CoreUser 생성만 (계좌 생성 및 가족 관계 등록 없음)</li>
     * </ul>
     *
     * @param req 사용자 초기화 요청 정보 (Role 포함)
     * @return 부모인 경우 ParentUserInitRes, 자녀인 경우 ChildUserInitRes
     * @throws BusinessException 이미 등록된 사용자인 경우 (CONFLICT)
     * @throws BusinessException 잘못된 Role인 경우 (UNAUTHORIZED)
     */
    UserInitRes initChannelUser(ChannelUserInitReq req);

}
