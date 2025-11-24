package dev.syntax.domain.user.controller;

import dev.syntax.domain.user.dto.ChannelUserInitReq;
import dev.syntax.domain.user.dto.ChannelUserInitRes;
import dev.syntax.domain.user.service.InitService;
import dev.syntax.global.response.ApiResponseUtil;
import dev.syntax.global.response.BaseResponse;
import dev.syntax.global.response.SuccessCode;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * 은행 업무 관련 컨트롤러
 * <p>
 * 사용자 초기화 등 은행 핵심 업무를 처리합니다.
 * </p>
 */
@RestController
@RequestMapping("/core/banking")
@RequiredArgsConstructor
public class BankingController {

    private final InitService  initService;

    /**
     * 부모 사용자 초기화 API
     * <p>
     * 부모 사용자 가입 시 CoreUser 생성, 계좌 생성, 초기 잔액 입금을 처리합니다.
     * </p>
     *
     * @param request 사용자 초기화 요청 정보
     * @return 생성된 사용자 ID와 계좌 정보
     */
    @PostMapping("/init")
    public ResponseEntity<BaseResponse<?>> createCoreUser(@RequestBody ChannelUserInitReq request){
        ChannelUserInitRes response = initService.initChannelParentUser(request);
        return ApiResponseUtil.success(SuccessCode.OK, response);
    }
}
