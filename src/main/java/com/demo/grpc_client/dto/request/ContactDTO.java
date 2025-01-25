package com.demo.grpc_client.dto.request;

import lombok.Builder;
import lombok.Data;

import java.util.List;
import java.util.Map;

@Builder
@Data
public class ContactDTO {

    private String phone;
    private String mobile;
    private String workPhone;
    private List<String> emails;
    private Map<String, String> socialMedia;

}