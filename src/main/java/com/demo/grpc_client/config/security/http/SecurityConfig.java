package com.demo.grpc_client.config.security.http;

import com.demo.grpc_client.config.security.common.JwtAuthenticationService;
import com.demo.grpc_client.config.security.server.ServerAuthenticationFilter;
import com.demo.grpc_client.config.security.server.ServerAuthenticationService;
import com.demo.grpc_client.config.security.server.ServerTokenClaims;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.AuthenticationServiceException;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

// HTTP Request → JwtAuthenticationFilter → TokenExtractor → JwtAuthenticationService
@RequiredArgsConstructor
@Configuration
public class SecurityConfig {

    private final JwtAuthenticationFilter jwtAuthenticationFilter;
    private final JwtAuthenticationService jwtAuthenticationService;
    private final ServerAuthenticationFilter serverAuthenticationFilter;
    private final ServerAuthenticationService serverAuthenticationService;

    @Bean
    public AuthenticationManager authenticationManager() {
        return authentication -> {
            // 서버 인증 토큰인 경우
            if (authentication.getPrincipal() instanceof ServerTokenClaims) {
                return serverAuthenticationService.authenticateServer((ServerTokenClaims) authentication.getPrincipal());
            }

            // 사용자 JWT 토큰인 경우
            if (authentication.getPrincipal() instanceof String token) {
                return jwtAuthenticationService.authenticateToken(token);
            }

            throw new AuthenticationServiceException("Unsupported authentication type");
        };
    }

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        return http
                .csrf(AbstractHttpConfigurer::disable)
                .sessionManagement(s -> s.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .authorizeHttpRequests(auth -> auth
                        // 서버 간 통신을 위한 엔드포인트
                        .requestMatchers("/api/server/**").hasRole("SERVER")
                        // 기존 사용자 인증 관련 엔드포인트
                        .requestMatchers("/api/auth/**").permitAll()
                        .requestMatchers("/api/auth/login").permitAll()
                        .requestMatchers("/api/auth/register").permitAll()
                        .requestMatchers("/api/members/**").permitAll()
                        .anyRequest().authenticated()
                )
                // 서버 인증 필터를 JWT 인증 필터보다 먼저 적용
                .addFilterBefore(serverAuthenticationFilter, UsernamePasswordAuthenticationFilter.class)
                .addFilterBefore(jwtAuthenticationFilter, UsernamePasswordAuthenticationFilter.class)
                .exceptionHandling(e -> e
                        .authenticationEntryPoint((request, response, authException) -> {
                            response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
                            response.getWriter().write(authException.getMessage());
                        })
                )
                .build();
    }

}
