package com.demo.grpc_client.controller.token;

import com.demo.grpc_client.dto.MemberSignUpRequestDTO;
import com.demo.grpc_client.service.MemberGrpcClientService;
import com.test.member.grpc.MemberProto;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/members")
@RequiredArgsConstructor
public class MemberController {
    private final MemberGrpcClientService memberGrpcClientService;

    @PostMapping
    public ResponseEntity<MemberProto.MemberCreateResponse> createMember(
            @RequestBody MemberSignUpRequestDTO request
    ) {
        // SecurityContext에서 인증 정보를 가져옵니다
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String username = authentication.getName();

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