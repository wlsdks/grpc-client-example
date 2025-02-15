package com.demo.grpc_client.client.grpc;

import com.test.member.grpc.MemberProto;
import com.test.member.grpc.MemberServiceGrpc;
import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;
import io.grpc.Channel;
import io.grpc.StatusRuntimeException;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import net.devh.boot.grpc.client.inject.GrpcClient;
import org.springframework.stereotype.Component;

@Slf4j
@RequiredArgsConstructor
@Component
public class GrpcMemberClientAnnotation {

    @GrpcClient("member-service")
    private Channel channel; // gRPC 채널 재사용

    // Stub을 재사용하기 위해 필드로 선언
    private MemberServiceGrpc.MemberServiceBlockingStub stub;

    @PostConstruct
    public void init() {
        // 채널로부터 Stub을 한 번만 생성
        stub = MemberServiceGrpc.newBlockingStub(channel);
    }

    /**
     * 회원 ID로 회원 조회
     *
     * @param memberId 조회할 회원의 ID
     * @return 조회된 회원 정보 DTO
     */
    @CircuitBreaker(name = "grpc-circuit", fallbackMethod = "getFallbackResponse")
    public MemberProto.MemberResponse getMemberById(Long memberId) {
        log.trace("getMemberById 메서드 진입 - 요청 ID: {}", memberId);

        // gRPC 요청 객체 생성
        MemberProto.MemberIdRequest request = MemberProto.MemberIdRequest.newBuilder()
                .setId(memberId)
                .build();

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
     * @return Fallback 응답
     * @apiNote 서킷 브레이커가 열렸을 때 반환할 Fallback 응답
     */
    public MemberProto.MemberResponse getFallbackResponse(Long memberId, Throwable t) {
        log.error("gRPC 호출 실패 또는 서킷 브레이커 오픈 - memberId={}, fallback 응답 반환", memberId);
        return MemberProto.MemberResponse.newBuilder()
                .setId(-1L) // 예시로 -1을 반환
                .setName("Fallback Member")
                .build();
    }

}