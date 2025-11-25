package dev.syntax.global.auth.resolver;

import dev.syntax.global.auth.annotation.CurrentUserId;
import org.springframework.core.MethodParameter;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.bind.support.WebDataBinderFactory;
import org.springframework.web.context.request.NativeWebRequest;
import org.springframework.web.method.support.HandlerMethodArgumentResolver;
import org.springframework.web.method.support.ModelAndViewContainer;

/**
 * {@link CurrentUserId} 어노테이션이 붙은 메서드 파라미터에
 * SecurityContext에 저장된 CoreUserId를 주입하는 ArgumentResolver입니다.
 * <p>
 * CoreUserIdFilter에서 SecurityContext에 저장한 Principal(CoreUserId)을 추출하여 제공합니다.
 * </p>
 *
 * @see CurrentUserId
 * @see dev.syntax.global.auth.filter.CoreUserIdFilter
 */
@Component
public class CurrentUserIdResolver implements HandlerMethodArgumentResolver {

    /**
     * 파라미터가 @CurrentUserId 어노테이션을 가지고 있는지 확인합니다.
     *
     * @param parameter 메서드 파라미터
     * @return @CurrentUserId 어노테이션이 있으면 true
     */
    @Override
    public boolean supportsParameter(MethodParameter parameter) {
        return parameter.hasParameterAnnotation(CurrentUserId.class) &&
                (parameter.getParameterType().equals(Long.class) || parameter.getParameterType().equals(long.class));
    }

    /**
     * SecurityContext에서 CoreUserId를 추출하여 반환합니다.
     *
     * @param parameter     메서드 파라미터
     * @param mavContainer  ModelAndViewContainer
     * @param webRequest    NativeWebRequest
     * @param binderFactory WebDataBinderFactory
     * @return SecurityContext에 저장된 CoreUserId
     * @throws IllegalStateException Authentication이 없거나 Principal이 Long 타입이 아닌 경우
     */
    @Override
    public Object resolveArgument(
            MethodParameter parameter,
            ModelAndViewContainer mavContainer,
            NativeWebRequest webRequest,
            WebDataBinderFactory binderFactory
    ) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

        if (authentication == null) {
            throw new IllegalStateException("Authentication이 SecurityContext에 존재하지 않습니다.");
        }

        Object principal = authentication.getPrincipal();

        if (!(principal instanceof Long)) {
            throw new IllegalStateException(
                    "Principal이 Long 타입이 아닙니다. 실제 타입: " + principal.getClass().getName()
            );
        }

        return principal;
    }
}
