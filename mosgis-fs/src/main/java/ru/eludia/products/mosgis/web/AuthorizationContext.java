package ru.eludia.products.mosgis.web;

import javax.ws.rs.core.SecurityContext;
import java.security.Principal;
import java.util.UUID;

public class AuthorizationContext implements SecurityContext, Principal {

    public AuthorizationContext(UUID senderUuid) {
        this.senderUuid = senderUuid;
    }

    public UUID getSenderUuid() {
        return senderUuid;
    }

    private UUID senderUuid;

    @Override
    public Principal getUserPrincipal() {
        return this;
    }

    @Override
    public boolean isUserInRole(String s) {
        return false;
    }

    @Override
    public boolean isSecure() {
        return true;
    }

    @Override
    public String getAuthenticationScheme() {
        return BASIC_AUTH;
    }

    @Override
    public String getName() {
        return senderUuid.toString();
    }
}
