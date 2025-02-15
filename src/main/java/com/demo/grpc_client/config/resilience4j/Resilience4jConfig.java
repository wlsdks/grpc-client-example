package com.demo.grpc_client.config.resilience4j;

import feign.FeignException;
import io.github.resilience4j.circuitbreaker.CircuitBreakerConfig;
import io.github.resilience4j.circuitbreaker.CircuitBreakerRegistry;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.net.ConnectException;

@Configuration
public class Resilience4jConfig {

    @Bean
    public CircuitBreakerConfig circuitBreakerConfig() {
        return CircuitBreakerConfig.custom()
                // 호출 횟수 기반 슬라이딩 윈도우 (최근 10회 호출 기준)
                .slidingWindowType(CircuitBreakerConfig.SlidingWindowType.COUNT_BASED)
                // 실패율 50% 이상이면 OPEN 상태로 전환
                .failureRateThreshold(50)
                // OPEN 상태에서 5초 동안 호출 차단 후 HALF_OPEN 상태로 전환
                .waitDurationInOpenState(java.time.Duration.ofSeconds(5))
                // HALF_OPEN 상태에서 최대 5개 호출 허용
                .permittedNumberOfCallsInHalfOpenState(5)
                // 최근 10회 호출을 기준으로 통계 집계
                .slidingWindowSize(10)
                // OPEN 상태에서 HALF_OPEN으로 자동 전환 활성화
                .automaticTransitionFromOpenToHalfOpenEnabled(true)
                // FeignException, ConnectException, RuntimeException을 실패 예외로 기록
                .recordExceptions(FeignException.class, ConnectException.class, RuntimeException.class)
                .build();
    }

    // 위 설정을 기반으로 CircuitBreakerRegistry를 생성
    @Bean
    public CircuitBreakerRegistry circuitBreakerRegistry(CircuitBreakerConfig config) {
        return CircuitBreakerRegistry.of(config);
    }

}
