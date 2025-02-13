package com.demo.grpc_client.service;

import com.demo.grpc_client.client.feign.MemberFeignClient;
import com.demo.grpc_client.dto.response.ResponseMemberDTO;
import feign.FeignException;
import io.github.resilience4j.circuitbreaker.CallNotPermittedException;
import io.github.resilience4j.circuitbreaker.CircuitBreaker;
import io.github.resilience4j.circuitbreaker.CircuitBreakerRegistry;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import java.net.ConnectException;

@Slf4j
@RequiredArgsConstructor
@Service
public class FindMemberService {

    private final MemberFeignClient memberFeignClient;
    private final CircuitBreakerRegistry circuitBreakerRegistry;
    private CircuitBreaker feignCircuitBreaker;

    @PostConstruct
    public void init() {
        // "member-service" 이름으로 서킷 브레이커 생성 (설정은 기본값 또는 별도 구성한 값 사용)
        feignCircuitBreaker = circuitBreakerRegistry.circuitBreaker("member-service");
    }

    /**
     * 회원 ID로 회원 조회 (서킷 브레이커 적용 + Fallback 추가)
     *
     * @param memberId 조회할 회원의 ID
     * @return 조회된 회원 정보 DTO (fallback 포함)
     */
    public ResponseMemberDTO findMemberInformation(Long memberId) {
        try {
            return feignCircuitBreaker.executeSupplier(() -> {
                try {
                    // FeignClient로 원격 호출 수행
                    ResponseEntity<ResponseMemberDTO> response = memberFeignClient.getMemberById(memberId);

                    // HTTP 응답 코드가 2xx가 아니면 예외 발생
                    if (!response.getStatusCode().is2xxSuccessful()) {
                        throw new RuntimeException("Feign call failed with status: " + response.getStatusCode());
                    }

                    return response.getBody();
                } catch (FeignException e) {
                    log.error("[FEIGN EXCEPTION] CircuitBreaker 감지 - memberId={}, 원인={}", memberId, e.getMessage());
                    throw e; // CircuitBreaker가 감지할 수 있도록 예외를 다시 던짐
                }
            });
        } catch (CallNotPermittedException e) {
            log.error("[CIRCUITBREAKER OPEN] 요청 차단됨 - memberId={}, 원인={}", memberId, e.getMessage());
            return fallbackMemberInfo(memberId, e);
        }
    }

    /**
     * Fallback 메서드: FeignClient 호출 실패 시 대체 응답 반환
     *
     * @param memberId 조회할 회원 ID
     * @param t        실패 원인
     * @return 대체 회원 정보 DTO
     */
    public ResponseMemberDTO fallbackMemberInfo(Long memberId, Throwable t) {
        log.error("[FEIGN FALLBACK] CircuitBreaker 발동 - memberId={}, 원인={}", memberId, t.getMessage());

        // 기본 응답 생성 (예시)
        return ResponseMemberDTO.builder()
                .id(-1L)  // -1을 설정하여 실패한 데이터임을 나타냄
                .name("Fallback User")
                .email("fallback@example.com")
                .build();
    }
}
