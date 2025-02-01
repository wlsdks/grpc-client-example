package com.demo.grpc_client.controller;

import com.demo.grpc_client.service.GrpcStreamingService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RequestMapping("/test")
@RequiredArgsConstructor
@RestController
public class GrpcStreamingController {

    private final GrpcStreamingService grpcStreamingService;

    /**
     * gRPC 스트리밍 요청 테스트를 실행합니다.
     *
     * @return 스트리밍 요청 결과 메시지
     * @apiNote 1. HTTP POST 요청 /test/streaming으로 전달.
     * 2. 내부적으로 GrpcStreamingService가 GrpcStreamingClient를 호출하여 스트리밍 동작 수행.
     * 3. gRPC 응답 결과를 받아, 결과 메시지를 HTTP로 반환.
     */
    @PostMapping("/streaming")
    public ResponseEntity<String> executeStreamingTest() {
        String result = grpcStreamingService.executeStreamingTest();
        return ResponseEntity.ok(result);
    }

}
