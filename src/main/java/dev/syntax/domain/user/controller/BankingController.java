package dev.syntax.domain.user.controller;

import dev.syntax.domain.user.dto.ChannelUserInitReq;
import dev.syntax.domain.user.dto.UserInitRes;
import dev.syntax.domain.user.service.InitService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/core/banking")
@RequiredArgsConstructor
public class BankingController {

    private final InitService initService;

    /**
     * 사용자 생성 API
     * <p>
     * 요청의 Role에 따라 부모와 자녀를 구분하여 처리합니다.
     * </p>
     * <ul>
     *   <li>PARENT: CoreUser 생성, 계좌 생성, 초기 잔액 100만원 입금</li>
     *   <li>CHILD: CoreUser 생성만 (계좌 생성 및 가족 관계 등록 없음)</li>
     * </ul>
     *
     * @param request 사용자 생성 요청 정보 (Role 포함)
     * @return 부모인 경우 ParentUserInitRes, 자녀인 경우 ChildUserInitRes
     */
    @PostMapping("/init")
    @ResponseStatus(HttpStatus.CREATED)
    public UserInitRes createCoreUser(@Valid @RequestBody ChannelUserInitReq request) {
        return initService.initChannelUser(request);
    }
}
