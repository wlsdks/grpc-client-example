package com.demo.grpc_client.config.security.server;

import lombok.Getter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Getter
@ConfigurationProperties(prefix = "server")  // application.yml의 'server' 프리픽스와 매칭
@Component
public class ServerProperties {

    private ServerType type;  // server.type과 자동으로 매핑됨
    private String id;  // server.id와 매핑됨

}