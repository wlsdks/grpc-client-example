package com.demo.grpc_client.dto.request;

import lombok.Builder;
import lombok.Data;

import java.util.Map;

@Builder
@Data
public class AddressDTO {

    private String street;
    private String city;
    private String country;
    private String postalCode;
    private Map<String, String> additionalInfo;

}