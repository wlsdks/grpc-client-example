package com.demo.grpc_client.service;

import com.demo.grpc_client.client.feign.MemberFeignClient;
import com.demo.grpc_client.dto.response.ResponseMemberDTO;
import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

@Slf4j
@RequiredArgsConstructor
@Service
public class FindMemberServiceAnnotation {

    private final MemberFeignClient memberFeignClient;

    @CircuitBreaker(name = "member-service", fallbackMethod = "fallbackMemberInfo")
    public ResponseMemberDTO findMemberInformation(Long memberId) {
        ResponseEntity<ResponseMemberDTO> response = memberFeignClient.getMemberById(memberId);

        if (!response.getStatusCode().is2xxSuccessful()) {
            throw new RuntimeException("Feign call failed with status: " + response.getStatusCode());
        }

        return response.getBody();
    }

    public ResponseMemberDTO fallbackMemberInfo(Long memberId, Throwable t) {
        // fallback 메서드 내에서 로그 출력
        log.error("[FEIGN FALLBACK] CircuitBreaker 발동 - memberId={}, 원인={}", memberId, t.getMessage(), t);

        return ResponseMemberDTO.builder()
                .id(-1L)
                .name("Fallback User")
                .email("fallback@example.com")
                .build();
    }

}
