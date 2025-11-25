package dev.syntax.domain.account.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import dev.syntax.domain.account.dto.DepositAccountReq;
import dev.syntax.domain.account.entity.Account;
import dev.syntax.domain.account.enums.AccountStatus;
import dev.syntax.domain.account.enums.AccountType;
import dev.syntax.domain.account.service.AccountService;
import dev.syntax.domain.user.entity.CoreUser;
import dev.syntax.global.TestSecurityConfig;
import dev.syntax.global.exception.BusinessException;
import dev.syntax.global.filter.ReadinessFilter;
import dev.syntax.global.response.error.ErrorBaseCode;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.FilterType;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;
import java.time.LocalDate;

import static org.mockito.BDDMockito.given;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(
        controllers = AccountController.class,
        excludeFilters = @ComponentScan.Filter(
                type = FilterType.ASSIGNABLE_TYPE,
                classes = ReadinessFilter.class
        )
)
@Import(TestSecurityConfig.class)
class AccountControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockitoBean
    private AccountService accountService;

    /* ------------------------------------------------------
        성공 케이스
    ------------------------------------------------------ */
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

        given(accountService.createChildDepositAccount(1L, req))
                .willReturn(account);

        // when & then
        mockMvc.perform(post("/core/banking/account/create")
                        .header("X-Core-User-Id", "1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data").exists())
                .andExpect(jsonPath("$.data.accountId").value(2))
                .andExpect(jsonPath("$.data.accountNumber").value("1687-807-144644"))
                .andExpect(jsonPath("$.data.accountType").value("DEPOSIT"))
                .andExpect(jsonPath("$.data.balance").value("0"));
    }

    /* ------------------------------------------------------
        실패 케이스 - 부모 없음
    ------------------------------------------------------ */
    @Test
    @DisplayName("자녀 계좌 생성 실패 - 부모 없음")
    void createDepositAccount_fail_parentNotFound() throws Exception {

        DepositAccountReq req = new DepositAccountReq(1L, 2L, AccountType.DEPOSIT);

        given(accountService.createChildDepositAccount(1L, req))
                .willThrow(new BusinessException(ErrorBaseCode.PARENT_USER_NOT_FOUND));

        mockMvc.perform(post("/core/banking/account/create")
                        .header("X-Core-User-Id", "1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.message").exists());
    }

    /* ------------------------------------------------------
        실패 케이스 - 자녀 없음
    ------------------------------------------------------ */
    @Test
    @DisplayName("자녀 계좌 생성 실패 - 자녀 없음")
    void createDepositAccount_fail_childNotFound() throws Exception {

        DepositAccountReq req = new DepositAccountReq(1L, 2L, AccountType.DEPOSIT);

        given(accountService.createChildDepositAccount(1L, req))
                .willThrow(new BusinessException(ErrorBaseCode.CHILD_USER_NOT_FOUND));

        mockMvc.perform(post("/core/banking/account/create")
                        .header("X-Core-User-Id", "1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.message").exists());
    }

    /* ------------------------------------------------------
        실패 케이스 - 관계 이미 존재
    ------------------------------------------------------ */
    @Test
    @DisplayName("자녀 계좌 생성 실패 - 가족 관계 이미 존재")
    void createDepositAccount_fail_relationshipExists() throws Exception {

        DepositAccountReq req = new DepositAccountReq(1L, 2L, AccountType.DEPOSIT);

        given(accountService.createChildDepositAccount(1L, req))
                .willThrow(new BusinessException(ErrorBaseCode.CONFLICT));

        mockMvc.perform(post("/core/banking/account/create")
                        .header("X-Core-User-Id", "1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.message").exists());
    }
}
