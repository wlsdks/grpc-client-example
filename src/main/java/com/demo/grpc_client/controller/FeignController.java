package com.demo.grpc_client.controller;

import com.demo.grpc_client.client.feign.MemberFeignClient;
import com.demo.grpc_client.dto.response.ResponseMemberDTO;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@Slf4j
@RequiredArgsConstructor
@RestController
@RequestMapping("/api/test")
public class FeignController {

    private final MemberFeignClient memberFeignClient;

    /**
     * [FEIGN] HTTP 요청 -> Feign(REST) -> gRPC 서버(내부에서 HTTP API가 있다 가정) -> 응답
     *
     * @param memberId 조회할 회원 ID
     * @return ResponseEntity<ResponseMemberDTO>
     */
    @GetMapping("/feign")
    public ResponseEntity<ResponseMemberDTO> feignTest(@RequestParam Long memberId) {
        log.trace("[FEIGN TEST] 들어온 HTTP 요청 - memberId={}", memberId);

        // 1) FeignClient로 원격 서버의 /api/members/{memberId} 호출
        ResponseEntity<ResponseMemberDTO> response;
        try {
            response = memberFeignClient.getMemberById(memberId);
        } catch (Exception e) {
            log.error("[FEIGN TEST] 요청 중 예외 발생 - {}", e.getMessage());
            return ResponseEntity.internalServerError().build();
        }

        // 2) 응답 확인 로깅
        if (response.getBody() != null) {
            log.trace("[FEIGN TEST] 응답 - ID={}, email={}",
                    response.getBody().getId(),
                    response.getBody().getEmail());
        } else {
            log.warn("[FEIGN TEST] 응답 바디가 없음");
        }

        // 3) 최종 HTTP 응답
        return ResponseEntity.ok(response.getBody());
    }

}
