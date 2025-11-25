package dev.syntax.global;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.util.StringUtils;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.Collections;

@Configuration
public class TestSecurityConfig {

    @Bean
    public SecurityFilterChain testSecurity(HttpSecurity http) throws Exception {
        return http
                .csrf(csrf -> csrf.disable())
                .authorizeHttpRequests(auth -> auth.anyRequest().permitAll())
                .addFilterBefore(new TestCoreUserIdFilter(), UsernamePasswordAuthenticationFilter.class)
                .build();
    }

    /**
     * 테스트용 CoreUserIdFilter
     * X-Core-User-Id 헤더를 읽어 SecurityContext에 Long 타입 principal로 저장합니다.
     */
    private static class TestCoreUserIdFilter extends OncePerRequestFilter {

        private static final String CORE_USER_ID = "X-Core-User-Id";

        @Override
        protected void doFilterInternal(
                HttpServletRequest request,
                HttpServletResponse response,
                FilterChain filterChain
        ) throws ServletException, IOException {

            String userId = request.getHeader(CORE_USER_ID);

            if (StringUtils.hasText(userId)) {
                try {
                    Long coreUserId = Long.parseLong(userId);
                    UsernamePasswordAuthenticationToken authentication =
                            new UsernamePasswordAuthenticationToken(coreUserId, null, Collections.emptyList());
                    SecurityContextHolder.getContext().setAuthentication(authentication);
                } catch (NumberFormatException ignored) {
                    // 테스트에서는 무시
                }
            }

            filterChain.doFilter(request, response);
        }
    }
}
