package com.demo.grpc_client.client.grpc;

import com.test.member.grpc.MemberProto;
import com.test.member.grpc.MemberServiceGrpc;
import io.grpc.Channel;
import io.grpc.StatusRuntimeException;
import io.grpc.stub.StreamObserver;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import net.devh.boot.grpc.client.inject.GrpcClient;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;

@Slf4j
@RequiredArgsConstructor
@Component
public class GrpcMemberClient {

    @GrpcClient("member-service")
    private Channel channel; // gRPC 채널 재사용

    /**
     * 회원 ID로 회원 조회
     *
     * @param memberId 조회할 회원의 ID
     * @return 조회된 회원 정보 DTO
     */
    public MemberProto.MemberResponse getMemberById(Long memberId) {
        log.trace("getMemberById 메서드 진입 - 요청 ID: {}", memberId);

        // 1. 블로킹 Stub 생성 (동기 호출)
        MemberServiceGrpc.MemberServiceBlockingStub stub =
                MemberServiceGrpc.newBlockingStub(channel);

        // 2. gRPC 요청 객체 생성
        MemberProto.MemberIdRequest request = MemberProto.MemberIdRequest.newBuilder()
                .setId(memberId)
                .build();

        // 현재 인증 정보 로깅
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        log.trace("Current Authentication: {}", authentication);

        // 토큰 로깅
        if (authentication != null && authentication.getCredentials() != null) {
//            log.info("Current Token: {}", authentication.getCredentials());
        }

        try {
            return stub.getMemberById(request);
        } catch (StatusRuntimeException e) {
            log.error("gRPC 호출 실패 - 상태: {}, 설명: {}, 원인: {}",
                    e.getStatus(),
                    e.getStatus().getDescription(),
                    e.getCause());
            throw e;
        }
    }

    /**
     * 비동기 회원 조회 메서드 (선택적 구현)
     *
     * @param memberId         조회할 회원의 ID
     * @param responseObserver 응답을 처리할 StreamObserver
     */
    public void getMemberByIdAsync(
            Long memberId,
            StreamObserver<MemberProto.MemberResponse> responseObserver
    ) {
        // 1. 비동기 Stub 생성
        MemberServiceGrpc.MemberServiceStub asyncStub =
                MemberServiceGrpc.newStub(channel);

        // 2. gRPC 요청 객체 생성
        MemberProto.MemberIdRequest request = MemberProto.MemberIdRequest.newBuilder()
                .setId(memberId)
                .build();

        // 3. 비동기 요청
        asyncStub.getMemberById(request, responseObserver);
    }

}