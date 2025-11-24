package dev.syntax.global.auth;

import org.springframework.security.authentication.AbstractAuthenticationToken;
import org.springframework.security.core.GrantedAuthority;

import java.util.Collection;

/**
 * API Key 기반 인증 토큰입니다.
 * principal에는 호출 시스템 이름(예: "CHANNEL")을 저장합니다.
 */
public class ApiKeyAuthenticationToken extends AbstractAuthenticationToken {

    private final Object principal;

    /**
     * API Key 인증 토큰을 생성합니다.
     *
     * @param principal   호출 시스템 이름 (예: "CHANNEL_SERVER")
     * @param authorities 권한 목록
     */
    public ApiKeyAuthenticationToken(Object principal, Collection<? extends GrantedAuthority> authorities) {
        super(authorities);
        this.principal = principal;
        setAuthenticated(true);
    }

    @Override
    public Object getCredentials() {
        return "";
    }

    @Override
    public Object getPrincipal() {
        return principal;
    }
}
