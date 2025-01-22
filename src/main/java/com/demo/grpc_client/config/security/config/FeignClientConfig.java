package com.demo.grpc_client.config.security.config;

import com.demo.grpc_client.config.security.server.ServerProperties;
import com.demo.grpc_client.config.security.common.ServerTokenUtil;
import feign.RequestInterceptor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class FeignClientConfig {

    @Bean
    public RequestInterceptor serverAuthenticationInterceptor(ServerTokenUtil serverTokenUtil,
                                                              ServerProperties serverProperties) {
        return requestTemplate -> {
            // 서버 토큰 생성
            String token = serverTokenUtil.generateServerToken(
                    serverProperties.getType(),
                    serverProperties.getId()
            );

            // 서버 토큰을 헤더에 추가
            requestTemplate.header("Server-Authorization", "Server " + token);
        };
    }

}