package com.demo.grpc_client.controller.token;

import com.demo.grpc_client.dto.MemberSignUpRequestDTO;
import com.demo.grpc_client.service.MemberGrpcClientService;
import com.test.member.grpc.MemberProto;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Slf4j
@RequestMapping("/api/members")
@RequiredArgsConstructor
@RestController
public class MemberController {

    private final MemberGrpcClientService memberGrpcClientService;

    @PostMapping
    public ResponseEntity<MemberProto.MemberCreateResponse> createMember(
            @RequestBody MemberSignUpRequestDTO request
    ) {
        log.debug("Received member creation request for email: {}", request.getEmail());

        // gRPC 요청 객체 생성
        MemberProto.MemberRequest grpcRequest = MemberProto.MemberRequest.newBuilder()
                .setEmail(request.getEmail())
                .setPassword(request.getPassword())
                .setName(request.getName())
                .setProfileImageBase64(request.getProfileImageBase64())
                .setEtcInfo(request.getEtcInfo())
                .build();

        // 토큰은 gRPC 클라이언트 서비스 내부에서 SecurityContext를 통해 접근할 수 있습니다
        return ResponseEntity.ok(memberGrpcClientService.createMember(grpcRequest));
    }

}