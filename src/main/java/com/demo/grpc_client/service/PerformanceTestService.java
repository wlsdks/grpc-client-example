package com.demo.grpc_client.service;

import com.demo.grpc_client.client.GrpcMemberClient;
import com.demo.grpc_client.client.MemberFeignClient;
import com.demo.grpc_client.dto.MemberSignUpRequestDTO;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

@Slf4j
@RequiredArgsConstructor
@Service
public class PerformanceTestService {

    private final MemberFeignClient feignClient;
    private final GrpcMemberClient grpcClient;

    public void runPerformanceTest(int totalRequests, int concurrentRequests) {
        // HTTP/Feign 테스트
        Instant httpStart = Instant.now();
        runHttpTest(totalRequests, concurrentRequests);
        Duration httpDuration = Duration.between(httpStart, Instant.now());
        log.info("HTTP 테스트 완료: {}ms", httpDuration.toMillis());

        // gRPC 테스트
        Instant grpcStart = Instant.now();
        runGrpcTest(totalRequests, concurrentRequests);
        Duration grpcDuration = Duration.between(grpcStart, Instant.now());
        log.info("gRPC 테스트 완료: {}ms", grpcDuration.toMillis());

        // 결과 비교
        log.info("성능 비교:");
        log.info("HTTP 평균 처리 시간: {}ms", httpDuration.toMillis() / totalRequests);
        log.info("gRPC 평균 처리 시간: {}ms", grpcDuration.toMillis() / totalRequests);
    }

    private void runHttpTest(int totalRequests, int concurrentRequests) {
        ExecutorService executorService = Executors.newFixedThreadPool(concurrentRequests);
        List<CompletableFuture<Void>> futures = new ArrayList<>();

        for (int i = 0; i < totalRequests; i++) {
            futures.add(CompletableFuture.runAsync(() -> {
                MemberSignUpRequestDTO request = createTestRequest();
                feignClient.createMember(request);
            }, executorService));
        }

        CompletableFuture.allOf(futures.toArray(new CompletableFuture[0])).join();
        executorService.shutdown();
    }

    private void runGrpcTest(int totalRequests, int concurrentRequests) {
        ExecutorService executorService = Executors.newFixedThreadPool(concurrentRequests);
        List<CompletableFuture<Void>> futures = new ArrayList<>();

        for (int i = 0; i < totalRequests; i++) {
            futures.add(CompletableFuture.runAsync(() -> {
                MemberSignUpRequestDTO request = createTestRequest();
                grpcClient.createMember(request);
            }, executorService));
        }

        CompletableFuture.allOf(futures.toArray(new CompletableFuture[0])).join();
        executorService.shutdown();
    }

    private MemberSignUpRequestDTO createTestRequest() {
        return MemberSignUpRequestDTO.of(
                1L,
                "test@test.com",
                "test",
                "test",
                "test",
                "test");
    }

}