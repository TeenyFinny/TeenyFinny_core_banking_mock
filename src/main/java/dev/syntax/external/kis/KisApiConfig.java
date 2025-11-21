package dev.syntax.external.kis;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@Configuration
@ConfigurationProperties(prefix = "kis")
@Getter
@Setter
public class KisApiConfig {

    private String baseUrl;
    private String appKey;
    private String appSecret;
}