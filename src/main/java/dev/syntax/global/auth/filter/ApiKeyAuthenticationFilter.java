package dev.syntax.global.auth.filter;

import com.fasterxml.jackson.databind.ObjectMapper;
import dev.syntax.global.auth.ApiKeyAuthenticationToken;
import dev.syntax.global.auth.CoreSecurityProperties;
import dev.syntax.global.auth.SecurityConfig;
import dev.syntax.global.response.AuthErrorResponse;
import dev.syntax.global.response.BaseResponse;
import dev.syntax.global.response.error.ErrorAuthCode;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.util.AntPathMatcher;
import org.springframework.util.StringUtils;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.Collections;
import java.util.List;

import static java.security.MessageDigest.isEqual;

/**
 * API Key 기반으로 channel 서버를 인증하는 필터입니다.
 * <p>
 * X-API-KEY 헤더를 검증하고, 선택적으로 IP 화이트리스트를 확인합니다.
 * 인증 성공 시 SecurityContext에 {@link ApiKeyAuthenticationToken}을 설정합니다.
 * </p>
 */
public class ApiKeyAuthenticationFilter extends OncePerRequestFilter {

    private static final String API_KEY_HEADER = "X-API-KEY";

    private final CoreSecurityProperties securityProperties;
    private final ObjectMapper objectMapper;

    public ApiKeyAuthenticationFilter(CoreSecurityProperties securityProperties,
                                      ObjectMapper objectMapper) {
        this.securityProperties = securityProperties;
        this.objectMapper = objectMapper;
    }

    /**
     * 필터를 적용하지 않을 경로를 지정합니다.
     *
     * @param request HTTP 요청
     * @return 필터를 건너뛸 경우 true
     */
    @Override
    protected boolean shouldNotFilter(HttpServletRequest request) {
        AntPathMatcher matcher = new AntPathMatcher();
        String uri = request.getRequestURI();

        for (String openUri : SecurityConfig.PUBLIC_URIS) {
            if (matcher.match(openUri, uri)) {
                return true;
            }
        }
        return false;
    }


    @Override
    protected void doFilterInternal(
            HttpServletRequest request,
            HttpServletResponse response,
            FilterChain filterChain
    ) throws ServletException, IOException {

        if (SecurityContextHolder.getContext().getAuthentication() != null) {
            filterChain.doFilter(request, response);
            return;
        }

        String apiKey = request.getHeader(API_KEY_HEADER);
        if (!StringUtils.hasText(apiKey)) {
            writeError(response, ErrorAuthCode.UNAUTHORIZED);
            return;
        }

        String expected = securityProperties.getApiKey();
        if (!StringUtils.hasText(expected) || !isEqual(expected.getBytes(), apiKey.getBytes())) {
            writeError(response, ErrorAuthCode.UNAUTHORIZED);
            return;
        }

        List<String> allowedIps = securityProperties.getAllowedIps();

        if (securityProperties.isEnableIpFilter()
                && allowedIps != null
                && !allowedIps.isEmpty()) {

            String clientIp = request.getRemoteAddr(); // dev에서는 그냥 remoteAddr로 OK

            if (!allowedIps.contains(clientIp)) {
                writeError(response, ErrorAuthCode.ACCESS_DENIED);
                return;
            }
        }

        ApiKeyAuthenticationToken authentication =
                new ApiKeyAuthenticationToken("CHANNEL_SERVER", Collections.emptyList());
        SecurityContextHolder.getContext().setAuthentication(authentication);

        filterChain.doFilter(request, response);
    }

    /**
     * 인증 오류 응답을 작성합니다.
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
