package com.demo.grpc_client.controller;

import io.github.resilience4j.circuitbreaker.CircuitBreaker;
import io.github.resilience4j.circuitbreaker.CircuitBreakerRegistry;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.HashMap;
import java.util.Map;

@RequestMapping("/api/circuit-breaker")
@RequiredArgsConstructor
@RestController
public class CircuitBreakerController {

    private final CircuitBreakerRegistry circuitBreakerRegistry;

    @GetMapping
    public ResponseEntity<Map<String, Object>> getCircuitBreakerInfo() {
        Map<String, Object> info = new HashMap<>();

        // 등록된 모든 서킷 브레이커 정보 조회
        for (CircuitBreaker cb : circuitBreakerRegistry.getAllCircuitBreakers()) {
            Map<String, Object> cbInfo = new HashMap<>();
            cbInfo.put("state", cb.getState().toString());
            cbInfo.put("failureRate", cb.getMetrics().getFailureRate());
            cbInfo.put("bufferedCalls", cb.getMetrics().getNumberOfBufferedCalls());
            cbInfo.put("failedCalls", cb.getMetrics().getNumberOfFailedCalls());
            info.put(cb.getName(), cbInfo);
        }

        return ResponseEntity.ok(info);
    }

}
