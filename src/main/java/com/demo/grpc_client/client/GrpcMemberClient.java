package com.demo.grpc_client.client;

import com.demo.grpc_client.dto.MemberSignUpRequestDTO;
import com.test.member.grpc.MemberProto;
import com.test.member.grpc.MemberServiceGrpc;
import lombok.RequiredArgsConstructor;
import net.devh.boot.grpc.client.inject.GrpcClient;
import org.springframework.stereotype.Component;

@RequiredArgsConstructor
@Component
public class GrpcMemberClient {

    // @GrpcClient 어노테이션을 사용하여 gRPC 서버에 연결
    @GrpcClient("member-service")
    private MemberServiceGrpc.MemberServiceBlockingStub memberStub;

    /**
     * 회원 생성
     *
     * @param dto : MemberSignUpRequestDTO
     */
    public void createMember(MemberSignUpRequestDTO dto) {
        MemberProto.MemberRequest request = MemberProto.MemberRequest.newBuilder()
                .setEmail(dto.getEmail())
                .setPassword(dto.getPassword())
                .setName(dto.getName())
                .setProfileImageBase64("a".repeat(1024 * 1024)) // 1MB 데이터 전송
                .setEtcInfo(dto.getEtcInfo())
                .build();

        // 단일 요청 전송
        memberStub.createMember(request);
    }

}