package dev.syntax.global.channel;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

/**
 * Channel 서버 API 통신 설정을 관리하는 클래스입니다.
 * application.yml의 channel.api.* 설정을 바인딩합니다.
 */
@Getter
@Setter
@Component
@ConfigurationProperties(prefix = "channel.api")
public class ChannelApiProperties {

    /**
     * Channel 서버의 기본 URL
     */
    private String baseUrl;

    /**
     * Channel 서버 인증용 API 키 (필요 시)
     */
    private String apiKey;
}
