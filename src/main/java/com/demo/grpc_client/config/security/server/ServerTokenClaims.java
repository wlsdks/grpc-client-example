package com.demo.grpc_client.config.security.server;

public record ServerTokenClaims(
    ServerType serverType,
    String serverId
) {}