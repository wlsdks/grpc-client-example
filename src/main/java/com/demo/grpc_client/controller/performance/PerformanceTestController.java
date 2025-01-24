package com.demo.grpc_client.controller.performance;

import com.demo.grpc_client.client.feign.MemberFeignClient;
import com.demo.grpc_client.client.grpc.GrpcMemberClient;
import com.demo.grpc_client.dto.ResponseMemberDTO;
import com.test.member.grpc.MemberProto;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/**
 * gRPC 클라이언트 서버에서, HTTP 요청 -> (Feign or gRPC) -> 원격 gRPC 서버
 * 시나리오를 테스트하기 위한 컨트롤러.
 */
@Slf4j
@RequiredArgsConstructor
@RestController
@RequestMapping("/api/test")
public class PerformanceTestController {

    private final MemberFeignClient memberFeignClient;
    private final GrpcMemberClient grpcMemberClient;

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

    /**
     * [gRPC] HTTP 요청 -> gRPC 호출 -> 원격 gRPC 서버 -> 응답
     *
     * @param memberId 조회할 회원 ID
     * @return ResponseEntity<ResponseMemberDTO>
     */
    @GetMapping("/grpc")
    public ResponseEntity<ResponseMemberDTO> grpcTest(@RequestParam Long memberId) {
        log.trace("[gRPC TEST] 들어온 HTTP 요청 - memberId={}", memberId);

        // 1) gRPC 클라이언트 호출
        MemberProto.MemberResponse grpcResponse;
        try {
            grpcResponse = grpcMemberClient.getMemberById(memberId);
        } catch (Exception e) {
            log.error("[gRPC TEST] 요청 중 예외 발생 - {}", e.getMessage());
            return ResponseEntity.internalServerError().build();
        }

        // 2) gRPC 응답을 DTO로 변환
        ResponseMemberDTO result = ResponseMemberDTO.builder()
                .id(grpcResponse.getId())
                .email(grpcResponse.getEmail())
                .name(grpcResponse.getName())
                .build();

        log.trace("[gRPC TEST] 응답 - ID={}, email={}", result.getId(), result.getEmail());

        // 3) 최종 HTTP 응답
        return ResponseEntity.ok(result);
    }

}
