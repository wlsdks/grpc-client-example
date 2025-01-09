package com.demo.grpc_client.controller;

import com.demo.grpc_client.service.PerformanceTestService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RequestMapping("/test")
@RequiredArgsConstructor
@RestController
public class PerformanceTestController {

    private final PerformanceTestService performanceTestService;

    /**
     * 성능 테스트 실행 (일반 테스트)
     *
     * @param totalRequests      총 요청 수 (기본값: 10000)
     * @param concurrentRequests 동시 요청 수 (기본값: 100)
     * @return 테스트 결과 메시지
     */
    @PostMapping("/performance")
    public ResponseEntity<String> runPerformanceTest(
            @RequestParam(defaultValue = "1000") int totalRequests,
            @RequestParam(defaultValue = "100") int concurrentRequests) {
        performanceTestService.runPerformanceTest(totalRequests, concurrentRequests);
        return ResponseEntity.ok("성능 테스트가 완료되었습니다. 로그를 확인해주세요.");
    }

}