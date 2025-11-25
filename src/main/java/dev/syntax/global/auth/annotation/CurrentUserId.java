package dev.syntax.global.auth.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * SecurityContext에 저장된 CoreUserId를 메서드 파라미터로 주입받기 위한 어노테이션입니다.
 * <p>
 * CoreUserIdFilter에서 X-Core-User-Id 헤더를 검증하고 SecurityContext에 저장한 값을
 * 컨트롤러에서 쉽게 사용할 수 있도록 합니다.
 * </p>
 *
 * <h3>사용 예시:</h3>
 * <pre>{@code
 * @GetMapping("/accounts")
 * public ResponseEntity<?> getAccounts(@CurrentUserId Long userId) {
 *     // userId는 X-Core-User-Id 헤더값
 *     return accountService.getUserAccounts(userId);
 * }
 * }</pre>
 *
 * @see dev.syntax.global.auth.filter.CoreUserIdFilter
 * @see dev.syntax.global.auth.resolver.CurrentUserIdResolver
 */
@Target(ElementType.PARAMETER)
@Retention(RetentionPolicy.RUNTIME)
public @interface CurrentUserId {
}
