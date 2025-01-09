package com.demo.grpc_client.client;

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
    private MemberServiceGrpc.MemberServiceBlockingStub memberStub;

    /**
     * @apiNote 압축된 요청을 생성하여 서버로 전송하는 메서드
     */
    public void createMemberWithCompression() {
        Metadata metadata = new Metadata();
        metadata.put(Metadata.Key.of("grpc-encoding", Metadata.ASCII_STRING_MARSHALLER), "gzip");

        Channel channel = ClientInterceptors.intercept(memberStub.getChannel(), MetadataUtils.newAttachHeadersInterceptor(metadata));
        MemberServiceGrpc.MemberServiceBlockingStub compressedStub = MemberServiceGrpc.newBlockingStub(channel);

        // 요청 생성 및 전송
        MemberProto.MemberRequest request = MemberProto.MemberRequest.newBuilder()
                .setEmail("compressed@test.com")
                .setPassword("compressed_password")
                .setName("Compressed User")
                .setProfileImageBase64("Base64Data")
                .setEtcInfo("Compressed Info")
                .build();

        MemberProto.MemberCreateResponse response = compressedStub.createMember(request);
//        System.out.println("압축 응답: " + response);
    }

}