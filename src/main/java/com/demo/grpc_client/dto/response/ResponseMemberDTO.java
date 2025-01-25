package com.demo.grpc_client.dto.response;

import com.demo.grpc_client.dto.request.AddressDTO;
import com.demo.grpc_client.dto.request.ContactDTO;
import lombok.*;

import java.util.Set;

@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@Builder
@Getter
public class ResponseMemberDTO {

    private Long id;
    private String email;
    private String name;
    private String profileImageBase64;
    private AddressDTO address;
    private ContactDTO contact;
    private Set<String> interests;
    private Set<String> skills;
    private String metadata;

}