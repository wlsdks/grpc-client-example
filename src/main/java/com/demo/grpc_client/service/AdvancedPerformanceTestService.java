package com.demo.grpc_client.service;

import com.demo.grpc_client.client.GrpcCompressedClient;
import com.demo.grpc_client.client.GrpcMemberClient;
import com.demo.grpc_client.dto.MemberSignUpRequestDTO;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.stream.Collectors;

/**
 * AdvancedPerformanceTestService
 * <p>
 * 이 클래스는 gRPC와 gRPC 압축 요청의 성능 테스트를 실행합니다.
 * 다수의 요청에 대해 동시성을 테스트하고 요청별 응답 시간을 수집하여 통계를 제공합니다.
 */
@Slf4j
@RequiredArgsConstructor
@Service
public class AdvancedPerformanceTestService {

    private final GrpcMemberClient grpcClient;
    private final GrpcCompressedClient grpcCompressedClient;

    /**
     * @param totalRequests      총 요청 수
     * @param concurrentRequests 동시 요청 수
     * @apiNote gRPC 및 압축 gRPC 요청의 동시성 성능을 테스트합니다.
     */
    public void runAdvancedPerformanceTest(int totalRequests, int concurrentRequests) {
        log.info("대규모 동시성 테스트 시작 (총 요청 수: {}, 동시 요청 수: {})", totalRequests, concurrentRequests);

        // 일반 gRPC 테스트
        Instant grpcStart = Instant.now();
        List<Long> grpcLatencies = runGrpcTest(totalRequests, concurrentRequests);
        Duration grpcDuration = Duration.between(grpcStart, Instant.now());
        printStatistics("gRPC", grpcDuration, grpcLatencies);

        // 압축 gRPC 테스트
        Instant compressedStart = Instant.now();
        List<Long> compressedLatencies = runCompressedGrpcTest(totalRequests, concurrentRequests);
        Duration compressedDuration = Duration.between(compressedStart, Instant.now());
        printStatistics("Compressed gRPC", compressedDuration, compressedLatencies);
    }

    /**
     * @param totalRequests      총 요청 수
     * @param concurrentRequests 동시 요청 수
     * @return 각 요청의 응답 시간 목록 (밀리초 단위)
     * @apiNote 일반 gRPC 요청의 성능을 테스트합니다.
     */
    private List<Long> runGrpcTest(int totalRequests, int concurrentRequests) {
        return runTest(totalRequests, concurrentRequests, () -> {
            MemberSignUpRequestDTO request = createTestRequest();
            grpcClient.createMember(request); // 단일 요청 수행
        });
    }

    /**
     * @return 테스트용 MemberSignUpRequestDTO 객체
     * @apiNote 테스트 요청 데이터를 생성합니다.
     */
    private MemberSignUpRequestDTO createTestRequest() {
        String uniqueId = UUID.randomUUID().toString();
        return MemberSignUpRequestDTO.builder()
                .id(System.nanoTime()) // 고유한 ID 생성
                .email("test_" + uniqueId + "@test.com")
                .password("password_" + uniqueId)
                .name("name_" + uniqueId)
                .profileImageBase64("a".repeat(1024 * 1024) + uniqueId)
                .etcInfo("info_" + uniqueId)
                .build();
    }

    /**
     * @param totalRequests      총 요청 수
     * @param concurrentRequests 동시 요청 수
     * @return 각 요청의 응답 시간 목록 (밀리초 단위)
     * @apiNote 압축 gRPC 요청의 성능을 테스트합니다.
     */
    private List<Long> runCompressedGrpcTest(int totalRequests, int concurrentRequests) {
        return runTest(totalRequests, concurrentRequests, () -> {
            MemberSignUpRequestDTO request = createTestRequest();
            grpcCompressedClient.createMemberWithCompression(request); // 단일 요청 수행
        });
    }

    /**
     * @param totalRequests      총 요청 수
     * @param concurrentRequests 동시 요청 수
     * @param requestTask        요청 작업 (Runnable)
     * @return 각 요청의 응답 시간 목록 (밀리초 단위)
     * @apiNote 동시성 요청을 실행하고 응답 시간을 측정합니다.
     */
    private List<Long> runTest(int totalRequests, int concurrentRequests, Runnable requestTask) {
        ExecutorService executorService = Executors.newFixedThreadPool(concurrentRequests);
        List<CompletableFuture<Long>> futures = new ArrayList<>();
        List<Throwable> errors = new ArrayList<>(); // 실패 이유 저장

        for (int i = 0; i < totalRequests; i++) {
            futures.add(CompletableFuture.supplyAsync(() -> {
                Instant start = Instant.now();
                try {
                    requestTask.run();
                    return Duration.between(start, Instant.now()).toMillis();
                } catch (Exception e) {
                    errors.add(e); // 실패 이유 기록
                    log.error("요청 실패: ", e);
                    return null;
                }
            }, executorService));
        }

        List<Long> latencies = futures.stream()
                .map(CompletableFuture::join)
                .filter(Objects::nonNull) // 실패한 요청 제외
                .collect(Collectors.toList());

        executorService.shutdown();

        // 실패 이유 출력
        if (!errors.isEmpty()) {
            log.error("총 실패 요청 수: {}", errors.size());
            errors.forEach(error -> log.error("에러 세부 정보: ", error));
        }

        return latencies;
    }


    /**
     * @param protocol      테스트 프로토콜 (예: gRPC, Compressed gRPC)
     * @param totalDuration 총 실행 시간
     * @param latencies     각 요청의 응답 시간 목록 (밀리초 단위)
     * @apiNote 요청 성능 결과를 로그에 출력합니다.
     */
    private void printStatistics(String protocol, Duration totalDuration, List<Long> latencies) {
        log.info("=== {} 테스트 결과 ===", protocol);
        log.info("총 실행 시간: {}ms", totalDuration.toMillis());
        log.info("요청 수: {}", latencies.size());
        if (!latencies.isEmpty()) {
            double averageLatency = latencies.stream().mapToDouble(Long::doubleValue).average().orElse(0.0);
            long minLatency = latencies.stream().mapToLong(Long::longValue).min().orElse(0);
            long maxLatency = latencies.stream().mapToLong(Long::longValue).max().orElse(0);

            log.info("평균 응답 시간: {}ms", averageLatency);
            log.info("최소 응답 시간: {}ms", minLatency);
            log.info("최대 응답 시간: {}ms", maxLatency);

            int percentile95Index = (int) (latencies.size() * 0.95);
            int percentile99Index = (int) (latencies.size() * 0.99);

            List<Long> sortedLatencies = latencies.stream().sorted().collect(Collectors.toList());
            log.info("95번째 백분위수 응답 시간: {}ms", sortedLatencies.get(percentile95Index));
            log.info("99번째 백분위수 응답 시간: {}ms", sortedLatencies.get(percentile99Index));
        }
    }

}
