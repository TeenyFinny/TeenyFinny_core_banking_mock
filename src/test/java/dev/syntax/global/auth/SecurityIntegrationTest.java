package dev.syntax.global.auth;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@TestPropertySource(properties = {
        "core.security.api-key=test-api-key",
        "core.security.allowed-ips="
})
class SecurityIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Test
    @DisplayName("인증이 필요 없는 공개 엔드포인트 접근 성공")
    void accessPublicEndpoint_ShouldSucceed() throws Exception {

        String requestBody = """
                {
                    "channelUserId": 1,
                    "role": "PARENT",
                    "name": "홍길동",
                    "phoneNumber": "010-1234-5678",
                    "birthDate": "2010-01-01"
                }
                """;

        mockMvc.perform(post("/core/banking/init")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestBody))
                .andExpect(status().isOk());
    }


    @Test
    @DisplayName("API Key 없이 보호된 엔드포인트 접근 시 401 응답")
    void accessProtectedEndpoint_WithoutApiKey_ShouldReturnUnauthorized() throws Exception {

        mockMvc.perform(get("/core/banking/account"))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.errorCode").value("AUTH01"))
                .andExpect(jsonPath("$.message").exists());
    }

    @Test
    @DisplayName("잘못된 API Key로 보호된 엔드포인트 접근 시 401 응답")
    void accessProtectedEndpoint_WithInvalidApiKey_ShouldReturnUnauthorized() throws Exception {

        mockMvc.perform(get("/core/banking/account")
                        .header("X-API-KEY", "invalid-key"))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.errorCode").value("AUTH01"))
                .andExpect(jsonPath("$.message").exists());
    }

    @Test
    @DisplayName("API Key는 유효하지만 Core User Id 누락 시 400 응답")
    void accessProtectedEndpoint_WithoutCoreUserId_ShouldReturnBadRequest() throws Exception {

        mockMvc.perform(get("/core/banking/account")
                        .header("X-API-KEY", "test-api-key"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.errorCode").value("AUTH03"))
                .andExpect(jsonPath("$.message").exists());
    }

    @Test
    @DisplayName("API Key는 유효하지만 Core User Id 숫자 아님 → 400")
    void accessProtectedEndpoint_WithInvalidCoreUserId_ShouldReturnBadRequest() throws Exception {

        mockMvc.perform(get("/core/banking/account")
                        .header("X-API-KEY", "test-api-key")
                        .header("X-Core-User-Id", "invalid"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.errorCode").value("AUTH03"))
                .andExpect(jsonPath("$.message").exists());
    }

    @Test
    @DisplayName("모든 헤더가 유효할 때 보안 필터 통과 (401/403만 아니면 성공)")
    void accessProtectedEndpoint_WithValidHeaders_ShouldPassSecurity() throws Exception {

        int status = mockMvc.perform(get("/core/banking/account")
                        .header("X-API-KEY", "test-api-key")
                        .header("X-Core-User-Id", "123"))
                .andReturn()
                .getResponse()
                .getStatus();

        // 보안 필터를 통과했다면 401/403일 수 없음
        assert status != 401 && status != 403;
    }
}
