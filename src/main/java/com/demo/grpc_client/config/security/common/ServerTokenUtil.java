package com.demo.grpc_client.config.security.common;

import com.demo.grpc_client.config.security.server.ServerType;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import jakarta.annotation.PostConstruct;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.util.Date;

@Component
public class ServerTokenUtil {

    private static final String SECRET_KEY_STRING = "server_specific_secret_key_much_longer_than_user_token_key_for_security";
    private SecretKey secretKey;
    private final long expiration = 86400000; // 24시간 (서버 토큰은 더 긴 유효기간)

    @PostConstruct
    public void init() {
        secretKey = Keys.hmacShaKeyFor(SECRET_KEY_STRING.getBytes());
    }

    /**
     * @param serverType 서버 타입
     * @param serverId   서버 ID
     * @return 서버 토큰
     * @apiNote 서버 토큰을 생성합니다.
     */
    public String generateServerToken(ServerType serverType, String serverId) {
        return Jwts.builder()
                .subject(serverId)
                .claim("serverType", serverType.getCode())
                .issuedAt(new Date())
                .expiration(new Date(System.currentTimeMillis() + expiration))
                .signWith(secretKey, Jwts.SIG.HS256)
                .compact();
    }

}
