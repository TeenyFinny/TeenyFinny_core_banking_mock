package dev.syntax.global.auth;

import dev.syntax.global.TestSecurityConfig;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.FilterType;
import org.springframework.context.annotation.Import;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(controllers = TestPingController.class,
        excludeFilters = {
                @ComponentScan.Filter(
                        type = FilterType.ASSIGNABLE_TYPE,
                        classes = dev.syntax.global.filter.ReadinessFilter.class
                )
        }
)
@Import(TestSecurityConfig.class)
class SecurityIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    /**
     * 테스트 전용 컨트롤러
     * - CoreUserIdFilter가 SecurityContext에 저장한 principal(Long)을 그대로 반환한다.
     * - 실제 AccountController나 서비스 로직은 참여하지 않는다.
     */
    @RestController
    @RequestMapping("/core/banking")
    static class TestController {

        @GetMapping("/test/account")
        public ResponseEntity<Long> getAccount(Authentication authentication) {
            Long principal = authentication != null
                    ? (Long) authentication.getPrincipal()
                    : null;
            return ResponseEntity.ok(principal);
        }

        @GetMapping("/ping")
        public ResponseEntity<String> ping(Authentication authentication) {
            Long principal = authentication != null
                    ? (Long) authentication.getPrincipal()
                    : null;
            return ResponseEntity.ok("pong:" + principal);
        }
    }

    @Test
    @DisplayName("X-Core-User-Id 유효 헤더 → principal 저장됨")
    void validCoreUserIdHeader_ShouldSetAuthenticationPrincipal() throws Exception {

        var result = mockMvc.perform(get("/core/banking/test/account")
                        .header("X-Core-User-Id", "10"))
                .andExpect(status().isOk())
                .andReturn();

        String body = result.getResponse().getContentAsString();
        assertThat(body).isEqualTo("10");
    }

    @Test
    @DisplayName("X-Core-User-Id 없이 접근해도 필터는 예외 없이 통과됨")
    void missingCoreUserIdHeader_ShouldNotFail() throws Exception {

        mockMvc.perform(get("/core/banking/test/account"))
                .andExpect(status().isOk());
    }

    @Test
    @DisplayName("X-Core-User-Id 숫자 아님 → 필터에서 무시되고 200 OK")
    void invalidCoreUserIdHeader_ShouldIgnore() throws Exception {

        mockMvc.perform(get("/core/banking/test/account")
                        .header("X-Core-User-Id", "abc"))
                .andExpect(status().isOk());
    }
}
