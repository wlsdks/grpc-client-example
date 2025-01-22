package com.demo.grpc_client.config.security.server;

import com.demo.grpc_client.exception.ServerAuthenticationException;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.Set;

@RequiredArgsConstructor
@Component
public class ServerAuthenticationFilter extends OncePerRequestFilter {

    private final ServerTokenUtil serverTokenUtil;
    private final Set<String> serverAuthPaths; // 서버 인증이 필요한 경로

    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain filterChain) throws ServletException, IOException {
        String path = request.getRequestURI();
        if (!serverAuthPaths.contains(path)) {
            filterChain.doFilter(request, response);
            return;
        }

        String authHeader = request.getHeader("Server-Authorization");
        if (authHeader == null || !authHeader.startsWith("Server ")) {
            response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
            return;
        }

        try {
            String token = authHeader.substring(7);
            ServerTokenClaims claims = serverTokenUtil.validateAndGetClaims(token);

            SecurityContextHolder.getContext().setAuthentication(
                    new ServerAuthenticationToken(token, claims)
            );

            filterChain.doFilter(request, response);
        } catch (ServerAuthenticationException e) {
            response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
            response.getWriter().write(e.getMessage());
        }
    }

}
