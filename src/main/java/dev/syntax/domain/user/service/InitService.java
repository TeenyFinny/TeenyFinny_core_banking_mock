package dev.syntax.domain.user.service;

import dev.syntax.domain.user.dto.ChannelUserInitReq;
import dev.syntax.domain.user.dto.ChannelUserInitRes;

/**
 * 사용자 초기화 서비스
 * <p>
 * 부모 사용자 가입 시 필요한 초기화 작업을 처리합니다.
 * </p>
 */
public interface InitService {
    /**
     * 부모 사용자 초기화를 처리합니다.
     *
     * @param req 사용자 초기화 요청 정보
     * @return 생성된 사용자 ID와 계좌 정보
     */
    ChannelUserInitRes initChannelParentUser(ChannelUserInitReq req);
}
