package com.demo.grpc_client.config.security.grpc;

import com.demo.grpc_client.config.security.common.ServerTokenUtil;
import com.demo.grpc_client.config.security.server.ServerProperties;
import io.grpc.*;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public class GrpcServerAuthenticationInterceptor implements ClientInterceptor {

    private final ServerTokenUtil serverTokenUtil;
    private final ServerProperties serverProperties;

    private static final Metadata.Key<String> SERVER_AUTH_KEY =
            Metadata.Key.of("Server-Authorization", Metadata.ASCII_STRING_MARSHALLER);

    /**
     * @apiNote 서버 토큰을 생성하여 요청 헤더에 추가하는 인터셉터
     */
    @Override
    public <ReqT, RespT> ClientCall<ReqT, RespT> interceptCall(MethodDescriptor<ReqT, RespT> method,
                                                               CallOptions callOptions,
                                                               Channel next) {
        // 다음 채널을 호출하여 ClientCall을 가져옵니다
        return new ForwardingClientCall.SimpleForwardingClientCall<ReqT, RespT>(
                next.newCall(method, callOptions)) {

            @Override
            public void start(Listener<RespT> responseListener, Metadata headers) {
                // 서버 토큰 생성
                String token = serverTokenUtil.generateServerToken(
                        serverProperties.getType(),
                        serverProperties.getId()
                );

                // 서버 토큰을 헤더에 추가
                headers.put(SERVER_AUTH_KEY, "Server " + token);

                // 다음 ClientCall을 호출
                super.start(responseListener, headers);
            }
        };
    }

}