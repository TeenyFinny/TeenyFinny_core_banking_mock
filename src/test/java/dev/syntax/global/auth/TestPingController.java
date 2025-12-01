package dev.syntax.global.auth;

import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/core/banking")
public class TestPingController {

    @GetMapping("/test/account")
    public ResponseEntity<Long> getAccount(Authentication auth) {
        Long principal = auth != null ? (Long) auth.getPrincipal() : null;
        return ResponseEntity.ok(principal);
    }

    @GetMapping("/ping")
    public ResponseEntity<String> ping(Authentication auth) {
        Long principal = auth != null ? (Long) auth.getPrincipal() : null;
        return ResponseEntity.ok("pong:" + principal);
    }
}
