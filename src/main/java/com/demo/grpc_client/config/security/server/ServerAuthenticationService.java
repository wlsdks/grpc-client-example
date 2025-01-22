package com.demo.grpc_client.config.security.server;

import com.demo.grpc_client.exception.ServerAuthenticationException;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

@RequiredArgsConstructor
@Service
public class ServerAuthenticationService {

    private final ServerTokenUtil serverTokenUtil;

    public void validateServerRequest() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (!(authentication instanceof ServerAuthenticationToken)) {
            throw new ServerAuthenticationException("Invalid server authentication");
        }

        // 서버 토큰 검증
        ServerTokenClaims claims = (ServerTokenClaims) authentication.getPrincipal();

        // 필요한 서버 타입 검증 추가로직 작성하기
    }

}
