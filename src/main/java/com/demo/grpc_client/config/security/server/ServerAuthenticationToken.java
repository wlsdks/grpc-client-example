package com.demo.grpc_client.config.security.server;

import org.springframework.security.authentication.AbstractAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;

import java.util.Collections;

public class ServerAuthenticationToken extends AbstractAuthenticationToken {

    private final String token;
    private final ServerTokenClaims serverTokenClaims;

    public ServerAuthenticationToken(String token, ServerTokenClaims serverTokenClaims) {
        super(Collections.singleton(new SimpleGrantedAuthority("ROLE_SERVER")));
        this.token = token;
        this.serverTokenClaims = serverTokenClaims;
        setAuthenticated(true);
    }

    @Override
    public Object getCredentials() {
        return null;
    }

    @Override
    public Object getPrincipal() {
        return null;
    }

}
