package ylab.bies.fileStorageService.util;

import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;
import org.springframework.stereotype.Component;

@Component
public class AccessTokenManager {

  public Jwt getJwtFromContext() {
    Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
    JwtAuthenticationToken authenticationToken = (JwtAuthenticationToken) authentication;
    return (Jwt) authenticationToken.getCredentials();
  }
}
