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
                .slidingWindowType(CircuitBreakerConfig.SlidingWindowType.COUNT_BASED) // 호출 횟수 기반
                .failureRateThreshold(50)                                  // 실패율 50% 이상일 때 Circuit Open
                .waitDurationInOpenState(java.time.Duration.ofSeconds(5))  // 5초 동안 Circuit Open 상태 유지 (이후 Half-Open)
                .permittedNumberOfCallsInHalfOpenState(5)                  // Half-Open 상태에서 최대 5개의 호출 허용
                .slidingWindowSize(10)                                     // 통계 집계에 사용할 최근 호출 횟수 (10개)
                .automaticTransitionFromOpenToHalfOpenEnabled(true) // Open -> Half-Open 자동 전환 활성화
                .recordExceptions(FeignException.class, ConnectException.class, RuntimeException.class)
                .build();
    }

    // 기본 설정으로 레지스트리 생성
    @Bean
    public CircuitBreakerRegistry circuitBreakerRegistry(CircuitBreakerConfig config) {
        return CircuitBreakerRegistry.of(config);
    }

}
