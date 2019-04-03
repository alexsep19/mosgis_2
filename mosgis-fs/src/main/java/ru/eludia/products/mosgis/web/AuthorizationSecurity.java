package ru.eludia.products.mosgis.web;

import javax.ws.rs.core.SecurityContext;
import java.security.Principal;
import java.util.UUID;

public class AuthorizationSecurity implements SecurityContext {

    public AuthorizationSecurity(UUID senderUuid) {
        this.senderUuid = senderUuid;
    }

    public UUID getSenderUuid() {
        return senderUuid;
    }

    private UUID senderUuid;

    @Override
    public Principal getUserPrincipal() {
        return null;
    }

    @Override
    public boolean isUserInRole(String s) {
        return false;
    }

    @Override
    public boolean isSecure() {
        return false;
    }

    @Override
    public String getAuthenticationScheme() {
        return null;
    }
}
