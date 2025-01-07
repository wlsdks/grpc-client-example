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

    @PostMapping("/performance")
    public ResponseEntity<String> runPerformanceTest(
            @RequestParam(defaultValue = "10000") int totalRequests,
            @RequestParam(defaultValue = "100") int concurrentRequests) {
        performanceTestService.runPerformanceTest(totalRequests, concurrentRequests);
        return ResponseEntity.ok("성능 테스트가 완료되었습니다. 로그를 확인해주세요.");
    }

}