package com.demo.grpc_client.dto;

import lombok.*;

@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@Builder
@Getter
public class ResponseMemberDTO {

    private Long id;
    private String email;
    private String password;
    private String name;
    private String profileImageBase64;
    private String etcInfo;

}