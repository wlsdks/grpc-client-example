package com.demo.grpc_client.client;

import com.demo.grpc_client.dto.MemberSignUpRequestDTO;
import com.test.member.grpc.MemberProto;
import com.test.member.grpc.MemberServiceGrpc;
import io.grpc.Channel;
import lombok.RequiredArgsConstructor;
import net.devh.boot.grpc.client.inject.GrpcClient;
import org.springframework.stereotype.Component;

@RequiredArgsConstructor
@Component
public class GrpcMemberClient {

    @GrpcClient("member-service")
    private Channel channel; // gRPC 채널 재사용

    /**
     * 회원 생성
     *
     * @param dto : MemberSignUpRequestDTO
     */
    public void createMember(MemberSignUpRequestDTO dto) {
        MemberServiceGrpc.MemberServiceBlockingStub stub = MemberServiceGrpc.newBlockingStub(channel);

        MemberProto.MemberRequest request = MemberProto.MemberRequest.newBuilder()
                .setId(dto.getId())
                .setEmail(dto.getEmail())
                .setPassword(dto.getPassword())
                .setName(dto.getName())
                .setProfileImageBase64(dto.getProfileImageBase64())
                .setEtcInfo(dto.getEtcInfo())
                .build();

        stub.createMember(request);
    }

}