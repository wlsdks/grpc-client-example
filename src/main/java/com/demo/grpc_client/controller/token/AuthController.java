package com.demo.grpc_client.controller.token;

import com.demo.grpc_client.config.security.common.JwtUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {

    private final JwtUtil jwtUtil;

    // 로그인 요청 DTO
    public record LoginRequest(String email, String password) {
    }

    // 로그인 응답 DTO
    public record LoginResponse(String accessToken, String tokenType) {
    }

    // 토큰 갱신 응답 DTO
    public record TokenResponse(String accessToken, String refreshToken) {
    }

    @PostMapping("/login")
    public ResponseEntity<LoginResponse> login(@RequestBody LoginRequest request) {
        // 실제로는 사용자 검증 로직이 필요합니다
        String token = jwtUtil.generateToken(request.email());
        return ResponseEntity.ok(new LoginResponse(token, "Bearer"));
    }

    @PostMapping("/refresh")
    public ResponseEntity<TokenResponse> refreshToken(
            @RequestHeader("Authorization") String refreshToken) {
        if (jwtUtil.isTokenValid(refreshToken)) {
            String username = jwtUtil.extractUsername(refreshToken);
            String newAccessToken = jwtUtil.generateToken(username);
            return ResponseEntity.ok(new TokenResponse(newAccessToken, refreshToken));
        }
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
    }

}