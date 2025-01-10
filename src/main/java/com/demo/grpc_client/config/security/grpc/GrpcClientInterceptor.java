package com.demo.grpc_client.config.security.grpc;

import io.grpc.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;

@Slf4j
@RequiredArgsConstructor
@Component
public class GrpcClientInterceptor implements ClientInterceptor {

    private static final Metadata.Key<String> AUTHORIZATION_HEADER =
            Metadata.Key.of("Authorization", Metadata.ASCII_STRING_MARSHALLER);

    /**
     * @param method      호출할 메서드
     * @param callOptions 호출 옵션
     * @param next        다음 채널
     * @param <ReqT>      요청 타입
     * @param <RespT>     응답 타입
     * @return ClientCall<ReqT, RespT>
     * @apiNote gRPC 요청에 토큰을 추가합니다.
     */
    @Override
    public <ReqT, RespT> ClientCall<ReqT, RespT> interceptCall(MethodDescriptor<ReqT, RespT> method,
                                                               CallOptions callOptions,
                                                               Channel next) {
        // 다음 채널을 호출하여 ClientCall을 가져옵니다
        return new ForwardingClientCall.SimpleForwardingClientCall<>(
                next.newCall(method, callOptions)) {

            @Override
            public void start(Listener<RespT> responseListener, Metadata headers) {
                // SecurityContext에서 인증 정보를 가져옵니다
                Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

                if (authentication != null && authentication.getCredentials() != null) {
                    String token = authentication.getCredentials().toString();
                    headers.put(AUTHORIZATION_HEADER, "Bearer " + token);
                    log.debug("Adding token to gRPC call for user: {}", authentication.getName());
                }

                super.start(responseListener, headers);
            }
        };
    }

}