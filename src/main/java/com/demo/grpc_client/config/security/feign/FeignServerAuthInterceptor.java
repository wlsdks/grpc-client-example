package com.demo.grpc_client.config.security.feign;

import com.demo.grpc_client.config.security.common.ServerTokenUtil;
import com.demo.grpc_client.config.security.server.ServerProperties;
import feign.RequestInterceptor;
import feign.RequestTemplate;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@RequiredArgsConstructor
@Component
public class FeignServerAuthInterceptor implements RequestInterceptor {

    private final ServerTokenUtil serverTokenUtil;
    private final ServerProperties serverProperties;

    @Override
    public void apply(RequestTemplate template) {
        // 서버 토큰 생성
        String token = serverTokenUtil.generateServerToken(
                serverProperties.getType(),
                serverProperties.getId()
        );

        // 토큰을 헤더에 추가 (gRPC와 동일한 헤더 이름 사용)
        template.header("Server-Authorization", "Server " + token);
    }

}
