package com.demo.grpc_client.dto.request;

import lombok.*;

@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@Builder
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
