package dev.syntax.domain.goal.client;

import dev.syntax.domain.goal.dto.GoalDepositEventReq;
import dev.syntax.global.channel.ChannelApiProperties;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpMethod;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

@Service
@RequiredArgsConstructor
public class ChannelGoalClient {

    private final RestTemplate restTemplate;
    private final ChannelApiProperties properties;

    private final String GOAL_DEPOSIT_EVENT_URL = "/internal/goal/deposit";

    /**
     * 목표계좌 입금 이벤트를 Channel 서버로 전송
     */
    public void sendGoalDepositEvent(GoalDepositEventReq req) {

        restTemplate.exchange(
                properties.getBaseUrl() + GOAL_DEPOSIT_EVENT_URL,
                HttpMethod.POST,
                new HttpEntity<>(req),
                Void.class
        );
    }
}
