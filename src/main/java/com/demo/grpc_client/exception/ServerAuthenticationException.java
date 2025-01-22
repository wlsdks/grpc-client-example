package com.demo.grpc_client.exception;

import lombok.AllArgsConstructor;
import lombok.Getter;

@AllArgsConstructor
@Getter
public class ServerAuthenticationException extends RuntimeException {

    private final String message;

}
