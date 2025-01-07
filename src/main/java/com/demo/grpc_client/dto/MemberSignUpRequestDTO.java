package com.demo.grpc_client.dto;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@Getter
public class MemberSignUpRequestDTO {

    private Long id;
    private String email;
    private String password;
    private String name;
    private String profileImageBase64;
    private String etcInfo;

    public static MemberSignUpRequestDTO of(Long id, String email, String password, String name, String profileImageBase64, String etcInfo) {
        return new MemberSignUpRequestDTO(id, email, password, name, profileImageBase64, etcInfo);
    }

}
