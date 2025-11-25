package dev.syntax.global.config;

import dev.syntax.global.auth.resolver.CurrentUserIdResolver;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.method.support.HandlerMethodArgumentResolver;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import java.util.List;

/**
 * Spring MVC 설정
 * <p>
 * CORS 설정 및 커스텀 ArgumentResolver를 등록합니다.
 * </p>
 */
@Configuration
@RequiredArgsConstructor
public class WebConfig implements WebMvcConfigurer {

    private final CurrentUserIdResolver currentUserIdResolver;

    /**
     * CORS 설정
     */
    @Override
    public void addCorsMappings(CorsRegistry r) {
        r.addMapping("/**")
                .allowedMethods("GET", "POST", "PUT", "DELETE")
                .allowedOrigins("http://localhost:3000");
    }

    /**
     * 커스텀 ArgumentResolver 등록
     * <p>
     * @CurrentUserId 어노테이션을 사용하여 SecurityContext에서
     * CoreUserId를 주입받을 수 있도록 합니다.
     * </p>
     */
    @Override
    public void addArgumentResolvers(List<HandlerMethodArgumentResolver> resolvers) {
        resolvers.add(currentUserIdResolver);
    }
}