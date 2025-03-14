package com.demo.grpc_client.config.security.filter;

import com.demo.grpc_client.config.security.service.JwtAuthenticationService;
import com.demo.grpc_client.config.security.common.TokenExtractor;
import com.demo.grpc_client.exception.JwtAuthenticationException;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

@RequiredArgsConstructor
@Component
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private final JwtAuthenticationService jwtAuthenticationService;
    private final TokenExtractor tokenExtractor;

    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain filterChain) throws ServletException, IOException {
        String authHeader = request.getHeader("Authorization");

        try {
            tokenExtractor.extractFromHeader(authHeader)
                    .ifPresent(token -> {
                        Authentication authentication = jwtAuthenticationService.authenticateToken(token);
                        SecurityContextHolder.getContext().setAuthentication(authentication);
                    });

            filterChain.doFilter(request, response);
        } catch (JwtAuthenticationException e) {
            response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
            response.getWriter().write(e.getMessage());
        }
    }

}

