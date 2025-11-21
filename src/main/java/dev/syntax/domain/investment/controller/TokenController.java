package dev.syntax.domain.investment.controller;

import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.client.RestTemplate;

import java.util.HashMap;
import java.util.Map;

@RestController
public class TokenController {

    @PostMapping("/getToken")
    public ResponseEntity<String> getAccessToken() {
        RestTemplate restTemplate = new RestTemplate();

        // 요청 URL
        String url = "https://openapi.koreainvestment.com:9443/oauth2/tokenP";

        // 요청 헤더
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);

        // 요청 바디
        Map<String, String> body = new HashMap<>();
        body.put("grant_type", "client_credentials");
        body.put("appkey", "발급받은_APP_KEY");
        body.put("appsecret", "발급받은_APP_SECRET");

        HttpEntity<Map<String, String>> request = new HttpEntity<>(body, headers);

        // POST 요청 전송
        ResponseEntity<String> response = restTemplate.postForEntity(url, request, String.class);
        return response;
    }
}

