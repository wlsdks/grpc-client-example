package com.demo.grpc_client.config.security.grpc;

import io.grpc.ClientInterceptor;
import net.devh.boot.grpc.client.interceptor.GrpcGlobalClientInterceptor;
import org.springframework.context.annotation.Configuration;

@Configuration
public class GrpcClientConfig {

    @GrpcGlobalClientInterceptor
    public ClientInterceptor tokenClientInterceptor() {
        return new GrpcClientInterceptor();
    }

}