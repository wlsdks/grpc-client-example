package com.demo.grpc_client.controller.token;

import com.demo.grpc_client.dto.MemberSignUpRequestDTO;
import com.demo.grpc_client.dto.ResponseMemberDTO;
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
    public ResponseEntity<ResponseMemberDTO> createMember(@RequestBody MemberSignUpRequestDTO request) {
        // gRPC 요청 객체 생성
        MemberProto.MemberRequest grpcRequest = MemberProto.MemberRequest.newBuilder()
                .setEmail(request.getEmail())
                .setPassword(request.getPassword())
                .setName(request.getName())
                .setProfileImageBase64(request.getProfileImageBase64())
                .setEtcInfo(request.getEtcInfo())
                .build();

        // gRPC 호출 및 응답을 DTO로 변환
        MemberProto.MemberCreateResponse grpcResponse = memberGrpcClientService.createMember(grpcRequest);

        // gRPC 응답을 우리의 DTO로 변환
        ResponseMemberDTO response = ResponseMemberDTO.builder()
                .id(grpcResponse.getId())
                .email(grpcResponse.getEmail())
                .name(grpcResponse.getName())
                .build();

        return ResponseEntity.ok(response);
    }

}