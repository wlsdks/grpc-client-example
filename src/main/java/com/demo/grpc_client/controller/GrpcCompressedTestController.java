package com.demo.grpc_client.controller;

import com.demo.grpc_client.service.AdvancedPerformanceTestService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RequestMapping("/test")
@RequiredArgsConstructor
@RestController
public class GrpcCompressedTestController {

    private final AdvancedPerformanceTestService advancedPerformanceTestService;

    /**
     * 성능 테스트를 실행합니다. (압축 테스트)
     *
     * @param totalRequests      총 요청 수 (기본값: 1000)
     * @param concurrentRequests 동시 요청 수 (기본값: 100)
     * @return 테스트 결과 메시지
     */
    @PostMapping("/advanced-performance")
    public ResponseEntity<String> runAdvancedPerformanceTest(
            @RequestParam(defaultValue = "1000") int totalRequests,
            @RequestParam(defaultValue = "100") int concurrentRequests
    ) {
        advancedPerformanceTestService.runAdvancedPerformanceTest(totalRequests, concurrentRequests);
        return ResponseEntity.ok("성능 테스트가 실행되었습니다. 로그를 확인하세요.");
    }

}