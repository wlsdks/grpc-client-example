package com.demo.grpc_client.mapper;

import com.demo.grpc_client.dto.request.AddressDTO;
import com.demo.grpc_client.dto.request.ContactDTO;
import com.demo.grpc_client.dto.response.ResponseMemberDTO;
import com.test.member.grpc.MemberProto;
import org.springframework.stereotype.Component;

import java.util.HashSet;

@Component
public class GrpcMemberMapper {

    public ResponseMemberDTO protoToDto(MemberProto.MemberResponse proto) {
        return ResponseMemberDTO.builder()
                .id(proto.getId())
                .email(proto.getEmail())
                .name(proto.getName())
                .profileImageBase64(proto.getProfileImageBase64())
                .address(mapAddress(proto.getAddress()))
                .contact(mapContact(proto.getContact()))
                .interests(new HashSet<>(proto.getInterestsList()))
                .skills(new HashSet<>(proto.getSkillsList()))
                .metadata(proto.getMetadata())
                .build();
    }

    private AddressDTO mapAddress(MemberProto.Address proto) {
        return AddressDTO.builder()
                .street(proto.getStreet())
                .city(proto.getCity())
                .country(proto.getCountry())
                .postalCode(proto.getPostalCode())
                .additionalInfo(proto.getAdditionalInfoMap())
                .build();
    }

    private ContactDTO mapContact(MemberProto.Contact proto) {
        return ContactDTO.builder()
                .phone(proto.getPhone())
                .mobile(proto.getMobile())
                .workPhone(proto.getWorkPhone())
                .emails(proto.getEmailsList())
                .socialMedia(proto.getSocialMediaMap())
                .build();
    }

}