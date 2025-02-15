package com.demo.grpc_client.client.feign;

import com.demo.grpc_client.dto.response.ResponseMemberDTO;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

@FeignClient(name = "member-service", url = "http://localhost:8090")
public interface MemberFeignClient {

    @GetMapping("/api/members/{memberId}")
    ResponseEntity<ResponseMemberDTO> getMemberById(@PathVariable("memberId") Long memberId);

}
