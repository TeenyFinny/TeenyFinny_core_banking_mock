package dev.syntax.global.auth;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

/**
 * Core 서버 보안 관련 설정값을 관리합니다.
 * - API Key
 * - 허용 IP 목록
 */
@Getter
@Setter
@Component
@ConfigurationProperties(prefix = "core.security")
public class CoreSecurityProperties {

    /**
     * channel 서버와 공유하는 API Key 값입니다.
     */
    private String apiKey;

    /**
     * IP필터링 여부
     */
    private boolean enableIpFilter = false;

    /**
     * 허용할 원격 IP 목록입니다.
     * 비어 있으면 IP 검증을 수행하지 않습니다.
     */
    private List<String> allowedIps = new ArrayList<>();
}
