package com.demo.grpc_client.client.grpc;

import com.test.member.grpc.MemberProto;
import com.test.member.grpc.MemberServiceGrpc;
import io.grpc.stub.StreamObserver;
import lombok.RequiredArgsConstructor;
import net.devh.boot.grpc.client.inject.GrpcClient;
import org.springframework.stereotype.Component;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

@RequiredArgsConstructor
@Component
public class GrpcStreamingClient {

    @GrpcClient("member-service")
    private MemberServiceGrpc.MemberServiceStub asyncStub;

    /**
     * @throws InterruptedException : CountDownLatch.await() 메서드에서 발생할 수 있는 InterruptedException
     * @apiNote 클라이언트에서 서버로 스트리밍 요청을 보내고, 서버에서 스트리밍 응답을 받는 메서드
     */
    public void streamCreateMember() throws InterruptedException {
        CountDownLatch latch = new CountDownLatch(1);

        StreamObserver<MemberProto.MemberRequest> requestObserver = asyncStub.streamCreateMember(
                new StreamObserver<>() {
                    @Override
                    public void onNext(MemberProto.MemberCreateResponse response) {
                        System.out.println("응답 수신: " + response);
                    }

                    @Override
                    public void onError(Throwable t) {
                        t.printStackTrace();
                        latch.countDown();
                    }

                    @Override
                    public void onCompleted() {
                        System.out.println("응답 완료");
                        latch.countDown();
                    }
                });

        // 여러 요청 메시지 전송
        for (int i = 1; i <= 10; i++) {
            MemberProto.MemberRequest request = MemberProto.MemberRequest.newBuilder()
                    .setId(i)
                    .setEmail("user" + i + "@test.com")
                    .setPassword("password" + i)
                    .setName("User" + i)
                    .setProfileImageBase64("ImageBase64Data")
                    .setEtcInfo("Info" + i)
                    .build();
            requestObserver.onNext(request);
        }

        // 스트리밍 종료
        requestObserver.onCompleted();

        // 모든 응답이 수신될 때까지 대기
        latch.await(1, TimeUnit.MINUTES);
    }

}
