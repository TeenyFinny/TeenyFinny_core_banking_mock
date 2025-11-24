package dev.syntax.global.auth.filter;

import com.fasterxml.jackson.databind.ObjectMapper;
import dev.syntax.global.response.AuthErrorResponse;
import dev.syntax.global.response.BaseResponse;
import dev.syntax.global.response.error.ErrorAuthCode;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.util.StringUtils;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

/**
 * Core 서버 API 호출 시 core-user-id 헤더를 강제하는 필터입니다.
 * - 헤더: X-Core-User-Id
 * - 예외 경로:
 *   - /core/banking/init
 *   - /test/**
 *
 * 현재는 헤더 존재 + 숫자 형식까지만 검증합니다.
 * (추후 필요 시 DB 조회로 확장 가능)
 */
public class CoreUserIdFilter extends OncePerRequestFilter {

    private static final String CORE_USER_ID = "X-Core-User-Id";

    private final ObjectMapper objectMapper;

    public CoreUserIdFilter(ObjectMapper objectMapper) {
        this.objectMapper = objectMapper;
    }

    @Override
    protected boolean shouldNotFilter(HttpServletRequest request) {
        String uri = request.getRequestURI();

        if ("/core/banking/init".equals(uri)) return true;
        if (uri.startsWith("/test/")) return true;

        return false;
    }

    @Override
    protected void doFilterInternal(
            HttpServletRequest request,
            HttpServletResponse response,
            FilterChain filterChain
    ) throws ServletException, IOException {

        String userId = request.getHeader(CORE_USER_ID);

        if (!StringUtils.hasText(userId)) {
            writeError(response, ErrorAuthCode.BAD_REQUEST);
            return;
        }

        try {
            Long.parseLong(userId);
        } catch (NumberFormatException ex) {
            writeError(response, ErrorAuthCode.BAD_REQUEST);
            return;
        }

        filterChain.doFilter(request, response);
    }

    /**
     * 검증 오류 응답을 작성합니다.
     *
     * @param response  HTTP 응답
     * @param errorCode 오류 코드
     * @throws IOException 응답 작성 실패 시
     */
    private void writeError(HttpServletResponse response, ErrorAuthCode errorCode)
            throws IOException {

        BaseResponse<?> body = AuthErrorResponse.of(errorCode);

        response.setStatus(errorCode.getHttpStatus().value());
        response.setContentType("application/json;charset=UTF-8");
        response.getWriter().write(objectMapper.writeValueAsString(body));
    }
}
