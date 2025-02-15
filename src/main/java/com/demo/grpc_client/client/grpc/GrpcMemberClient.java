package com.demo.grpc_client.client.grpc;

import com.test.member.grpc.MemberProto;
import com.test.member.grpc.MemberServiceGrpc;
import io.github.resilience4j.circuitbreaker.CircuitBreaker;
import io.github.resilience4j.circuitbreaker.CircuitBreakerRegistry;
import io.grpc.Channel;
import io.grpc.StatusRuntimeException;
import io.grpc.stub.StreamObserver;
import io.vavr.control.Try;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import net.devh.boot.grpc.client.inject.GrpcClient;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;

import java.util.function.Supplier;

@Slf4j
@RequiredArgsConstructor
@Component
public class GrpcMemberClient {

    @GrpcClient("member-service")
    private Channel channel; // gRPC 채널 재사용

    // gRPC 클라이언트는 Feign처럼 자동 구성되는 것이 아니라 직접 호출 시점에 서킷 브레이커를 적용하는 방식으로 구성해야 합니다.
    private final CircuitBreakerRegistry circuitBreakerRegistry;
    private CircuitBreaker grpcCircuitBreaker;

    // Stub을 재사용하기 위해 필드로 선언
    private MemberServiceGrpc.MemberServiceBlockingStub stub;

    @PostConstruct
    public void init() {
        // "grpcCircuitBreaker" 이름으로 서킷 브레이커 생성 (설정은 기본값 또는 별도 구성한 값 사용)
        grpcCircuitBreaker = circuitBreakerRegistry.circuitBreaker("grpcCircuitBreaker");
        // 채널로부터 Stub을 한 번만 생성
        stub = MemberServiceGrpc.newBlockingStub(channel);
    }

    /**
     * 회원 ID로 회원 조회
     *
     * @param memberId 조회할 회원의 ID
     * @return 조회된 회원 정보 DTO
     */
    public MemberProto.MemberResponse getMemberById(Long memberId) {
        log.trace("getMemberById 메서드 진입 - 요청 ID: {}", memberId);

        Supplier<MemberProto.MemberResponse> decoratedSupplier =
                CircuitBreaker.decorateSupplier(grpcCircuitBreaker, () -> performGrpcCall(memberId));

        // Vavr 라이브러리의 Try 클래스를 사용하여 예외 처리
        return Try.ofSupplier(decoratedSupplier)
                .recover(throwable -> {
                    log.error("gRPC 호출 실패 또는 서킷 브레이커 오픈 - memberId={}, fallback 응답 반환", memberId);
                    return getFallbackResponse();
                })
                .get();
    }

    /**
     * @param memberId 조회할 회원 ID
     * @return gRPC 호출 결과
     * @apiNote gRPC 호출을 수행하는 메서드
     */
    private MemberProto.MemberResponse performGrpcCall(Long memberId) {
        // gRPC 요청 객체 생성
        MemberProto.MemberIdRequest request = MemberProto.MemberIdRequest.newBuilder()
                .setId(memberId)
                .build();

        // 현재 인증 정보 로깅 (SecurityContext에서 가져옴)
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        log.trace("Current Authentication: {}", authentication);

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
    private MemberProto.MemberResponse getFallbackResponse() {
        return MemberProto.MemberResponse.newBuilder()
                .setId(-1L) // 예시로 -1을 반환
                .setName("Fallback Member")
                .build();
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
        MemberServiceGrpc.MemberServiceStub asyncStub = MemberServiceGrpc.newStub(channel);

        // 2. gRPC 요청 객체 생성
        MemberProto.MemberIdRequest request = MemberProto.MemberIdRequest.newBuilder()
                .setId(memberId)
                .build();

        // 3. 비동기 요청
        asyncStub.getMemberById(request, responseObserver);
    }

}