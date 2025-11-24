package dev.syntax.domain.account.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import dev.syntax.domain.account.dto.DepositAccountReq;
import dev.syntax.domain.account.entity.Account;
import dev.syntax.domain.account.enums.AccountStatus;
import dev.syntax.domain.account.enums.AccountType;
import dev.syntax.domain.account.service.AccountService;
import dev.syntax.domain.user.entity.CoreUser;
import dev.syntax.domain.user.service.InitService;
import dev.syntax.global.exception.BusinessException;
import dev.syntax.global.filter.ReadinessFilter;
import dev.syntax.global.response.error.ErrorBaseCode;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.FilterType;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;
import java.time.LocalDate;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * 계좌 컨트롤러 테스트
 * <p>
 * 자녀 계좌 생성 API의 엔드포인트 분기, 상태코드, 서비스 호출 여부를 검증합니다.
 * </p>
 */
@WebMvcTest(
        controllers = AccountController.class,
        excludeFilters = @ComponentScan.Filter(
                type = FilterType.ASSIGNABLE_TYPE,
                classes = ReadinessFilter.class
        )
)
class AccountControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockitoBean
    private AccountService accountService;

    @MockitoBean
    private InitService initService;

    @Test
    @DisplayName("자녀 계좌 생성 성공")
    void createDepositAccount_success() throws Exception {
        // given
        DepositAccountReq req = new DepositAccountReq(1L, 2L, AccountType.DEPOSIT);

        CoreUser child = CoreUser.builder()
                .id(2L)
                .channelUserId(2L)
                .name("자녀")
                .phoneNumber("010-9876-5432")
                .birthDate(LocalDate.of(2010, 1, 1))
                .build();

        Account account = Account.builder()
                .id(2L)
                .user(child)
                .number("1687-807-144644")
                .productName("입출금 통장")
                .balance(BigDecimal.ZERO)
                .interestRate(new BigDecimal("0.001"))
                .status(AccountStatus.ACTIVE)
                .type(AccountType.DEPOSIT)
                .build();

        given(initService.createFamilyRelationship(any()))
                .willReturn(child);
        given(accountService.createDepositAccount(child))
                .willReturn(account);

        // when & then
        mockMvc.perform(post("/core/banking/account/create")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data").exists())
                .andExpect(jsonPath("$.data.accountId").value(2))
                .andExpect(jsonPath("$.data.accountNumber").value("1687-807-144644"))
                .andExpect(jsonPath("$.data.accountType").value("DEPOSIT"))
                .andExpect(jsonPath("$.data.balance").value("0")); // 문자열 비교
    }

    @Test
    @DisplayName("자녀 계좌 생성 실패 - 부모를 찾을 수 없음")
    void createDepositAccount_fail_parentNotFound() throws Exception {
        // given
        DepositAccountReq req = new DepositAccountReq(1L, 2L, AccountType.DEPOSIT);

        given(initService.createFamilyRelationship(any()))
                .willThrow(new BusinessException(ErrorBaseCode.USER_NOT_FOUND));

        // when & then
        mockMvc.perform(post("/core/banking/account/create")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.message").exists());
    }

    @Test
    @DisplayName("자녀 계좌 생성 실패 - 자녀를 찾을 수 없음")
    void createDepositAccount_fail_childNotFound() throws Exception {
        // given
        DepositAccountReq req = new DepositAccountReq(1L, 2L, AccountType.DEPOSIT);

        given(initService.createFamilyRelationship(any()))
                .willThrow(new BusinessException(ErrorBaseCode.USER_NOT_FOUND));

        mockMvc.perform(post("/core/banking/account/create")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.message").exists());
    }

    @Test
    @DisplayName("자녀 계좌 생성 실패 - 이미 존재하는 가족 관계")
    void createDepositAccount_fail_relationshipExists() throws Exception {
        // given
        DepositAccountReq req = new DepositAccountReq(1L, 2L, AccountType.DEPOSIT);

        given(initService.createFamilyRelationship(any()))
                .willThrow(new BusinessException(ErrorBaseCode.CONFLICT));

        mockMvc.perform(post("/core/banking/account/create")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.message").exists());
    }
}
