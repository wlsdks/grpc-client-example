package com.demo.grpc_client.controller;

import com.demo.grpc_client.client.GrpcMemberClient;
import com.demo.grpc_client.dto.ResponseMemberDTO;
import com.test.member.grpc.MemberProto;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Slf4j
@RequestMapping("/api/members")
@RequiredArgsConstructor
@RestController
public class MemberController {

    private final GrpcMemberClient grpcMemberClient;

    @GetMapping("/{memberId}")
    public ResponseEntity<ResponseMemberDTO> getMemberById(@PathVariable Long memberId) {
        // gRPC 호출
        MemberProto.MemberResponse grpcResponse = grpcMemberClient.getMemberById(memberId);

        // gRPC 응답을 우리의 DTO로 변환
        ResponseMemberDTO response = ResponseMemberDTO.builder()
                .id(grpcResponse.getId())
                .email(grpcResponse.getEmail())
                .name(grpcResponse.getName())
                .build();

        return ResponseEntity.ok(response);
    }

}