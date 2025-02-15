package com.demo.grpc_client.controller;

import com.demo.grpc_client.client.grpc.GrpcMemberClient;
import com.demo.grpc_client.client.grpc.GrpcMemberClientAnnotation;
import com.demo.grpc_client.dto.response.ResponseMemberDTO;
import com.demo.grpc_client.mapper.GrpcMemberMapper;
import com.test.member.grpc.MemberProto;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@Slf4j
@RequiredArgsConstructor
@RestController
@RequestMapping("/api/test")
public class GrpcController {

    private final GrpcMemberClient grpcMemberClient;
    private final GrpcMemberMapper grpcMemberMapper;
    private final GrpcMemberClientAnnotation grpcMemberClientAnnotation;

    /**
     * [gRPC] HTTP 요청 -> gRPC 호출 -> 원격 gRPC 서버 -> 응답
     *
     * @param memberId 조회할 회원 ID
     * @return ResponseEntity<ResponseMemberDTO>
     */
    @GetMapping("/grpc")
    public ResponseEntity<ResponseMemberDTO> grpcTest(@RequestParam Long memberId) {
        log.trace("[gRPC TEST] 들어온 HTTP 요청 - memberId={}", memberId);

        // 1) gRPC 클라이언트 호출
        try {
//            MemberProto.MemberResponse response = grpcMemberClient.getMemberById(memberId);
            MemberProto.MemberResponse response = grpcMemberClientAnnotation.getMemberById(memberId);

            ResponseMemberDTO responseMemberDTO = grpcMemberMapper.protoToDto(response);
            log.trace("[gRPC TEST] 응답 - ID={}, email={}", responseMemberDTO.getId(), responseMemberDTO.getEmail());

            return ResponseEntity.ok(responseMemberDTO);
        } catch (Exception e) {
            log.error("[gRPC TEST] 요청 중 예외 발생 - {}", e.getMessage());
            return ResponseEntity.internalServerError().build();
        }
    }

}
