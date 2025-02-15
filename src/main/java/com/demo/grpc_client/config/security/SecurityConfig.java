package com.demo.grpc_client.config.security;

import com.demo.grpc_client.config.security.filter.JwtAuthenticationFilter;
import com.demo.grpc_client.config.security.service.JwtAuthenticationService;
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

    @Bean
    public AuthenticationManager authenticationManager() {
        return authentication -> {
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
                        .requestMatchers("/api/auth/**").permitAll()
                        .requestMatchers("/api/auth/login").permitAll()
                        .requestMatchers("/api/auth/register").permitAll()
                        .requestMatchers("/api/members/**").permitAll()
                        .requestMatchers("/api/circuit-breaker").permitAll()
                        .anyRequest().authenticated()
                )
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
