package com.demo.grpc_client.config.security.service;

import lombok.RequiredArgsConstructor;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import java.util.Set;

/**
 * JWT 토큰 기반 인증을 위한 UserDetailsService 구현
 * 클라이언트 애플리케이션에서는 실제 사용자 정보를 조회하지 않고,
 * JWT 토큰에서 추출한 정보를 기반으로 UserDetails를 생성합니다.
 */
@RequiredArgsConstructor
@Service
public class CustomUserDetailsService implements UserDetailsService {

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        // 클라이언트에서는 실제 DB 조회 없이 토큰에서 추출한 username으로 UserDetails 생성
        return createUserDetails(username);
    }

    /**
     * 사용자 정보를 기반으로 UserDetails 객체를 생성합니다.
     * 클라이언트에서는 최소한의 정보만 포함하여 생성합니다.
     *
     * @param username JWT 토큰에서 추출한 사용자 식별자
     * @return UserDetails 구현체
     */
    private UserDetails createUserDetails(String username) {
        return User.builder()
                .username(username)
                .password("") // 클라이언트에서는 비밀번호 검증이 필요 없음
                .authorities(Set.of(new SimpleGrantedAuthority("ROLE_USER")))
                .build();
    }

}