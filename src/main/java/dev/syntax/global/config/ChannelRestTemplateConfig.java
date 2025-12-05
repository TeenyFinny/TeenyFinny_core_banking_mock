package dev.syntax.global.config;

import dev.syntax.global.channel.ChannelApiProperties;
import lombok.RequiredArgsConstructor;

import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.util.StringUtils;
import org.springframework.web.client.RestTemplate;

/**
 * Channel 서버 API 호출을 위한 RestTemplate 설정 클래스입니다.
 * <p>
 * 모든 요청에 X-API-KEY 헤더를 자동으로 추가합니다.
 * </p>
 */
@Configuration
@RequiredArgsConstructor
public class ChannelRestTemplateConfig {

	private final ChannelApiProperties channelApiProperties;

	/**
	 * Channel 서버 API 호출용 RestTemplate Bean을 생성합니다.
	 * <p>
	 * 모든 요청에 X-API-KEY 헤더를 자동으로 추가합니다.
	 * </p>
	 *
	 * @return Channel 서버 API 호출용 RestTemplate
	 */
	@Bean
	public RestTemplate restTemplate(RestTemplateBuilder builder) {
		return builder
			.additionalInterceptors((request, body, execution) -> {
				String apiKey = channelApiProperties.getApiKey();
				if (StringUtils.hasText(apiKey)) {
					request.getHeaders().add("X-API-KEY", apiKey);
				}
				return execution.execute(request, body);
			})
			.build();
	}
}
