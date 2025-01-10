package com.demo.grpc_client.service;

import com.test.member.grpc.MemberProto;
import com.test.member.grpc.MemberServiceGrpc;
import lombok.extern.slf4j.Slf4j;
import net.devh.boot.grpc.client.inject.GrpcClient;
import org.springframework.stereotype.Service;

@Slf4j
@Service
public class MemberGrpcClientService {

    @GrpcClient("member-service")
    private MemberServiceGrpc.MemberServiceBlockingStub blockingStub;

    public MemberProto.MemberCreateResponse createMember(MemberProto.MemberRequest request) {
        // 인터셉터가 자동으로 SecurityContext에서 토큰을 가져와 처리합니다
        return blockingStub.createMember(request);
    }

}