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
     * @param dto : MemberSignUpRequestDTO
     * @return MemberProto.MemberCreateResponse
     */
    public MemberProto.MemberCreateResponse createMember(MemberSignUpRequestDTO dto) {
        MemberProto.MemberRequest request = MemberProto.MemberRequest.newBuilder()
                .setEmail(dto.getEmail())
                .setPassword(dto.getPassword())
                .setName(dto.getName())
                .setProfileImageBase64(dto.getProfileImageBase64())
                .setEtcInfo(dto.getEtcInfo())
                .build();

        return memberStub.createMember(request);
    }

}