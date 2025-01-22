package com.demo.grpc_client.config.security.grpc;

import com.demo.grpc_client.config.security.server.ServerProperties;
import com.demo.grpc_client.config.security.server.ServerTokenUtil;
import io.grpc.ClientInterceptor;
import lombok.RequiredArgsConstructor;
import net.devh.boot.grpc.client.interceptor.GrpcGlobalClientInterceptor;
import org.springframework.context.annotation.Configuration;

@RequiredArgsConstructor
@Configuration
public class GrpcClientConfig {

    private final ServerTokenUtil serverTokenUtil;
    private final ServerProperties serverProperties;

    @GrpcGlobalClientInterceptor
    public ClientInterceptor userTokenInterceptor() {
        // 기존의 사용자 JWT 토큰을 처리하는 인터셉터
        return new GrpcClientInterceptor();
    }

    @GrpcGlobalClientInterceptor
    public ClientInterceptor serverTokenInterceptor() {
        // 새로 추가한 서버 간 인증 토큰을 처리하는 인터셉터
        return new GrpcServerAuthenticationInterceptor(serverTokenUtil, serverProperties);
    }

}