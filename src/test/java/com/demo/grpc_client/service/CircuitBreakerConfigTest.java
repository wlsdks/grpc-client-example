package com.demo.grpc_client.service;

import io.github.resilience4j.circuitbreaker.CircuitBreakerConfig;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

@DisplayName("Resilience4j CircuitBreaker 기본 설정 테스트")
class CircuitBreakerConfigTest {

    @DisplayName("CircuitBreakerConfig 기본값 검증")
    @Test
    void testDefaultConfigValues() {
        CircuitBreakerConfig circuitBreakerConfig = CircuitBreakerConfig.ofDefaults();

        // when: 각 설정 값 출력 (로그 확인)
        System.out.println("Failure Rate Threshold: " + circuitBreakerConfig.getFailureRateThreshold());
        System.out.println("Slow Call Rate Threshold: " + circuitBreakerConfig.getSlowCallRateThreshold());
        System.out.println("Slow Call Duration Threshold: " + circuitBreakerConfig.getSlowCallDurationThreshold().getSeconds());
        System.out.println("Permitted Number Of Calls In Half Open State: " + circuitBreakerConfig.getPermittedNumberOfCallsInHalfOpenState());
        System.out.println("Max Wait Duration In Half Open State: " + circuitBreakerConfig.getMaxWaitDurationInHalfOpenState().getSeconds());
        System.out.println("Sliding Window Type: " + circuitBreakerConfig.getSlidingWindowType());
        System.out.println("Sliding Window Size: " + circuitBreakerConfig.getSlidingWindowSize());
        System.out.println("Minimum Number Of Calls: " + circuitBreakerConfig.getMinimumNumberOfCalls());
        System.out.println("Wait Duration In Open State: " + circuitBreakerConfig.getWaitIntervalFunctionInOpenState().apply(1));
        System.out.println("Automatic Transition From Open To Half Open Enabled: " + circuitBreakerConfig.isAutomaticTransitionFromOpenToHalfOpenEnabled());
        System.out.println("Record Failure Predicate: " + circuitBreakerConfig.getRecordExceptionPredicate().test(new RuntimeException()));
        System.out.println("Ignore Exception Predicate: " + circuitBreakerConfig.getIgnoreExceptionPredicate().test(new RuntimeException()));
    }

}
