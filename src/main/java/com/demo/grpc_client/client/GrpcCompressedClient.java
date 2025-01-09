package com.demo.grpc_client.client;

import com.demo.grpc_client.dto.MemberSignUpRequestDTO;
import com.test.member.grpc.MemberProto;
import com.test.member.grpc.MemberServiceGrpc;
import io.grpc.Channel;
import io.grpc.ClientInterceptors;
import io.grpc.Metadata;
import io.grpc.stub.MetadataUtils;
import lombok.RequiredArgsConstructor;
import net.devh.boot.grpc.client.inject.GrpcClient;
import org.springframework.stereotype.Component;

@RequiredArgsConstructor
@Component
public class GrpcCompressedClient {

    @GrpcClient("member-service")
    private Channel channel;

    /**
     * @apiNote 압축된 요청을 생성하여 서버로 전송하는 메서드
     */
    public void createMemberWithCompression(MemberSignUpRequestDTO dto) {
        Metadata metadata = new Metadata();
        metadata.put(Metadata.Key.of("grpc-encoding", Metadata.ASCII_STRING_MARSHALLER), "gzip");

        // 채널에 압축 헤더 추가
        Channel compressedChannel = ClientInterceptors.intercept(channel, MetadataUtils.newAttachHeadersInterceptor(metadata));
        MemberServiceGrpc.MemberServiceBlockingStub compressedStub = MemberServiceGrpc.newBlockingStub(compressedChannel);

        // 요청 생성 및 전송
        MemberProto.MemberRequest request = MemberProto.MemberRequest.newBuilder()
                .setId(dto.getId())
                .setEmail(dto.getEmail())
                .setPassword(dto.getPassword())
                .setName(dto.getName())
                .setProfileImageBase64(dto.getProfileImageBase64())
                .setEtcInfo(dto.getEtcInfo())
                .build();

        compressedStub.createMember(request);
    }

}