package com.demo.grpc_client.service;

import com.demo.grpc_client.client.grpc.GrpcStreamingClient;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

/**
 * GrpcStreamingService
 * <p>
 * 이 서비스는 gRPC 스트리밍 요청을 실행하고 결과를 처리합니다.
 */
@Slf4j
@RequiredArgsConstructor
@Service
public class GrpcStreamingService {

    private final GrpcStreamingClient grpcStreamingClient;

    /**
     * @return 스트리밍 작업 결과 메시지
     * @apiNote gRPC 스트리밍 요청을 실행합니다.
     */
    public String executeStreamingTest() {
        try {
            grpcStreamingClient.streamCreateMember();
            return "gRPC 스트리밍 요청이 성공적으로 완료되었습니다.";
        } catch (InterruptedException e) {
            log.error("gRPC 스트리밍 요청 중 인터럽트 발생", e);
            return "gRPC 스트리밍 요청 중 오류가 발생했습니다.";
        }
    }

}
