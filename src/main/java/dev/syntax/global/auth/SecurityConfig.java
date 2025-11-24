package dev.syntax.global.auth;

import com.fasterxml.jackson.databind.ObjectMapper;
import dev.syntax.global.auth.filter.ApiKeyAuthenticationFilter;
import dev.syntax.global.auth.filter.CoreUserIdFilter;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

@Configuration
@EnableWebSecurity
@RequiredArgsConstructor
public class SecurityConfig {

    private final CoreSecurityProperties securityProperties;
    private final ObjectMapper objectMapper;
    public static final String[] PUBLIC_URIS = {"/core/banking/init", "/test/**"};

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {

        http
                .csrf(csrf -> csrf.disable())
                .sessionManagement(sm -> sm.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .formLogin(fl -> fl.disable())
                .httpBasic(hb -> hb.disable())
                .authorizeHttpRequests(auth -> auth
                        .requestMatchers(PUBLIC_URIS).permitAll()
                        .anyRequest().authenticated()
                );

        http.addFilterBefore(
                new ApiKeyAuthenticationFilter(securityProperties, objectMapper),
                UsernamePasswordAuthenticationFilter.class
        );

        http.addFilterAfter(
                new CoreUserIdFilter(objectMapper),
                ApiKeyAuthenticationFilter.class
        );

        return http.build();
    }
}
