package com.demo.grpc_client.controller;

import com.demo.grpc_client.dto.response.ResponseMemberDTO;
import com.demo.grpc_client.service.FindMemberService;
import com.demo.grpc_client.service.FindMemberServiceAnnotation;
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
public class FeignController {

    private final FindMemberService findMemberService;
    private final FindMemberServiceAnnotation findMemberServiceAnnotation;

    @GetMapping("/feign")
    public ResponseEntity<ResponseMemberDTO> feignTest(@RequestParam Long memberId) {
        log.trace("[FEIGN TEST] 들어온 HTTP 요청 - memberId={}", memberId);

        // 1) FeignClient로 원격 서버의 /api/members/{memberId} 호출
        ResponseMemberDTO response = findMemberServiceAnnotation.findMemberInformation(memberId);

        // 2) 최종 HTTP 응답
        return ResponseEntity.ok(response);
    }

}
