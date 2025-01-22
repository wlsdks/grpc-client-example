package com.demo.grpc_client.config.security.server;

import com.demo.grpc_client.exception.ServerAuthenticationException;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import java.util.Arrays;

@RequiredArgsConstructor
@Service
public class ServerAuthenticationService {

    private final ServerTokenUtil serverTokenUtil;

    public void validateServerRequest() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

        // instanceof 패턴 매칭을 사용하여 더 안전하게 체크
        if (!(authentication instanceof ServerAuthenticationToken serverToken)) {
            throw new ServerAuthenticationException("Server authentication required");
        }

        ServerTokenClaims claims = (ServerTokenClaims) serverToken.getPrincipal();

        // 서버 타입 검증
        if (!isValidServerType(claims.serverType())) {
            throw new ServerAuthenticationException("Invalid server type: " + claims.serverType());
        }

        // 추가적인 검증 로직
        validateServerClaims(claims);
    }

    public Authentication authenticateServer(ServerTokenClaims claims) {
        if (!isValidServerType(claims.serverType())) {
            throw new ServerAuthenticationException("Invalid server type: " + claims.serverType());
        }

        validateServerClaims(claims);
        return new ServerAuthenticationToken(claims);
    }

    private boolean isValidServerType(ServerType serverType) {
        return serverType != null && Arrays.asList(
                ServerType.CLIENT_SERVER,
                ServerType.AUTH_SERVER
        ).contains(serverType);
    }

    private void validateServerClaims(ServerTokenClaims claims) {
        // serverId가 null이거나 비어있지 않은지 확인
        if (claims.serverId() == null || claims.serverId().trim().isEmpty()) {
            throw new ServerAuthenticationException("Server ID is required");
        }

        // 여기에 추가적인 검증 로직 추가
        // 예: 허용된 서버 ID 목록과 대조
        // 예: 서버 상태 확인
        // 예: 서버 권한 레벨 확인
    }

}