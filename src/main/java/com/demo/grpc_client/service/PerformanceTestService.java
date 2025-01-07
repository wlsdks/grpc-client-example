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
import java.util.DoubleSummaryStatistics;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.stream.Collectors;

@Slf4j
@RequiredArgsConstructor
@Service
public class PerformanceTestService {

    private final MemberFeignClient feignClient;
    private final GrpcMemberClient grpcClient;

    public void runPerformanceTest(int totalRequests, int concurrentRequests) {
        // HTTP/Feign 테스트 실행 및 응답 시간 수집
        Instant httpStart = Instant.now();
        List<Long> httpLatencies = runHttpTest(totalRequests, concurrentRequests);
        Duration httpDuration = Duration.between(httpStart, Instant.now());

        // 잠시 대기하여 시스템 안정화
        try {
            Thread.sleep(1000);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }

        // gRPC 테스트 실행 및 응답 시간 수집
        Instant grpcStart = Instant.now();
        List<Long> grpcLatencies = runGrpcTest(totalRequests, concurrentRequests);
        Duration grpcDuration = Duration.between(grpcStart, Instant.now());

        // 상세한 통계 출력
        printDetailedStatistics("HTTP", httpDuration, httpLatencies, totalRequests);
        printDetailedStatistics("gRPC", grpcDuration, grpcLatencies, totalRequests);
    }

    private List<Long> runHttpTest(int totalRequests, int concurrentRequests) {
        ExecutorService executorService = Executors.newFixedThreadPool(concurrentRequests);
        List<CompletableFuture<Long>> futures = new ArrayList<>();

        for (int i = 0; i < totalRequests; i++) {
            futures.add(CompletableFuture.supplyAsync(() -> {
                Instant start = Instant.now();
                try {
                    MemberSignUpRequestDTO request = createTestRequest();
                    feignClient.createMember(request);
                    return Duration.between(start, Instant.now()).toMillis();
                } catch (Exception e) {
                    log.error("HTTP 요청 실패: ", e);
                    return -1L; // 실패한 요청 표시
                }
            }, executorService));
        }

        List<Long> latencies = futures.stream()
                .map(CompletableFuture::join)
                .filter(latency -> latency > 0) // 실패한 요청 제외
                .collect(Collectors.toList());

        executorService.shutdown();
        return latencies;
    }

    private List<Long> runGrpcTest(int totalRequests, int concurrentRequests) {
        ExecutorService executorService = Executors.newFixedThreadPool(concurrentRequests);
        List<CompletableFuture<Long>> futures = new ArrayList<>();

        for (int i = 0; i < totalRequests; i++) {
            futures.add(CompletableFuture.supplyAsync(() -> {
                Instant start = Instant.now();
                try {
                    MemberSignUpRequestDTO request = createTestRequest();
                    grpcClient.createMember(request);
                    return Duration.between(start, Instant.now()).toMillis();
                } catch (Exception e) {
                    log.error("gRPC 요청 실패: ", e);
                    return -1L; // 실패한 요청 표시
                }
            }, executorService));
        }

        List<Long> latencies = futures.stream()
                .map(CompletableFuture::join)
                .filter(latency -> latency > 0) // 실패한 요청 제외
                .collect(Collectors.toList());

        executorService.shutdown();
        return latencies;
    }

    private void printDetailedStatistics(String protocol, Duration totalDuration, List<Long> latencies, int totalRequests) {
        DoubleSummaryStatistics stats = latencies.stream()
                .mapToDouble(Long::doubleValue)
                .summaryStatistics();

        // 백분위수 계산을 위해 정렬
        List<Long> sortedLatencies = latencies.stream()
                .sorted()
                .collect(Collectors.toList());

        int p95Index = (int) (sortedLatencies.size() * 0.95);
        int p99Index = (int) (sortedLatencies.size() * 0.99);

        log.info("=== {} 테스트 결과 ===", protocol);
        log.info("총 실행 시간: {}ms", totalDuration.toMillis());
        log.info("총 요청 수: {}", totalRequests);
        log.info("성공한 요청 수: {}", latencies.size());
        log.info("실패한 요청 수: {}", totalRequests - latencies.size());
        log.info("평균 응답 시간: {:.2f}ms", stats.getAverage());
        log.info("최소 응답 시간: {}ms", stats.getMin());
        log.info("최대 응답 시간: {}ms", stats.getMax());
        log.info("95번째 백분위 응답 시간: {}ms", sortedLatencies.get(p95Index));
        log.info("99번째 백분위 응답 시간: {}ms", sortedLatencies.get(p99Index));
    }

    private MemberSignUpRequestDTO createTestRequest() {
        String uniqueId = UUID.randomUUID().toString();
        return MemberSignUpRequestDTO.of(
                System.nanoTime(), // 고유한 ID 생성
                "test_" + uniqueId + "@test.com",
                "password_" + uniqueId,
                "name_" + uniqueId,
                "profile_" + uniqueId,
                "info_" + uniqueId
        );
    }

}