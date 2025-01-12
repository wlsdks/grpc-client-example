package com.demo.grpc_client.client.feign;

import com.demo.grpc_client.dto.MemberSignUpRequestDTO;
import com.demo.grpc_client.dto.ResponseMemberDTO;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

@FeignClient(name = "member-service", url = "http://localhost:8090")
public interface MemberFeignClient {

    @PostMapping("/api/members")
    ResponseEntity<ResponseMemberDTO> createMember(@RequestBody MemberSignUpRequestDTO request);

    @GetMapping("/api/members/{memberId}")
    ResponseEntity<ResponseMemberDTO> getMemberById(@PathVariable("memberId") Long memberId);

}
