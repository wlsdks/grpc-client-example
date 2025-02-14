package com.demo.grpc_client.service;

import com.demo.grpc_client.client.feign.MemberFeignClient;
import com.demo.grpc_client.dto.response.ResponseMemberDTO;
import io.github.resilience4j.circuitbreaker.CallNotPermittedException;
import io.github.resilience4j.circuitbreaker.CircuitBreaker;
import io.github.resilience4j.circuitbreaker.CircuitBreakerRegistry;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

@Slf4j
@RequiredArgsConstructor
@Service
public class FindMemberService {

    private final MemberFeignClient memberFeignClient;
    private final CircuitBreakerRegistry circuitBreakerRegistry;
    private CircuitBreaker feignCircuitBreaker;

    @PostConstruct
    public void init() {
        feignCircuitBreaker = circuitBreakerRegistry.circuitBreaker("feignCircuitBreaker");
    }

    /**
     * 회원 ID로 회원 조회 (CircuitBreaker 적용)
     *
     * @param memberId 조회할 회원 ID
     * @return 조회된 회원 정보 (Fallback 포함)
     */
    public ResponseMemberDTO findMemberInformation(Long memberId) {
        try {
            return feignCircuitBreaker.executeSupplier(() -> fetchMemberInfo(memberId));
        } catch (CallNotPermittedException e) {
            log.warn("[CIRCUITBREAKER OPEN] 요청 차단됨 - memberId={}, 원인={}", memberId, e.getMessage());
            return fallbackMemberInfo(memberId, e);
        }
    }

    /**
     * FeignClient를 통해 회원 정보를 가져옴
     *
     * @param memberId 조회할 회원 ID
     * @return 회원 정보 DTO
     */
    private ResponseMemberDTO fetchMemberInfo(Long memberId) {
        ResponseEntity<ResponseMemberDTO> response = memberFeignClient.getMemberById(memberId);

        if (!response.getStatusCode().is2xxSuccessful()) {
            throw new RuntimeException("Feign call failed with status: " + response.getStatusCode());
        }

        return response.getBody();
    }

    /**
     * Fallback 메서드: FeignClient 호출 실패 시 대체 응답 반환
     *
     * @param memberId 조회할 회원 ID
     * @param t        실패 원인
     * @return 기본 회원 정보 DTO
     */
    private ResponseMemberDTO fallbackMemberInfo(Long memberId, Throwable t) {
        log.error("[FEIGN FALLBACK] CircuitBreaker 발동 - memberId={}, 원인={}", memberId, t.getMessage());

        return ResponseMemberDTO.builder()
                .id(-1L)
                .name("Fallback User")
                .email("fallback@example.com")
                .build();
    }

}
