package ylab.bies.userservice.service;

import org.keycloak.representations.AccessTokenResponse;
import org.keycloak.representations.idm.UserRepresentation;

import javax.ws.rs.core.Response;
import java.util.Set;

public interface KeycloakService {
    Response save(UserRepresentation user);

    void assignRoles(String userId, Set<String> roles);

    AccessTokenResponse getToken(String username, String password);
}
