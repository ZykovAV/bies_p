package ylab.bies.userservice.util;

import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;
import org.springframework.stereotype.Component;

import java.util.UUID;

@Component
public class AccessTokenManager {
    private static final String USER_ID_CLAIM = "sub";
    private static final String USERNAME_CLAIM = "preferred_username";

    public UUID getUserIdFromToken() {
        Jwt jwt = getJwtFromContext();
        return UUID.fromString((String) jwt.getClaims().get(USER_ID_CLAIM));
    }

    public String getUsernameFromToken() {
        Jwt jwt = getJwtFromContext();
        return (String) jwt.getClaims().get(USERNAME_CLAIM);
    }

    private Jwt getJwtFromContext() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        JwtAuthenticationToken authenticationToken = (JwtAuthenticationToken) authentication;
        return (Jwt) authenticationToken.getCredentials();
    }
}
