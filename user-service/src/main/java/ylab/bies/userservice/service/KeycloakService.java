package ylab.bies.userservice.service;

import org.keycloak.representations.AccessTokenResponse;
import org.keycloak.representations.idm.UserRepresentation;

import javax.ws.rs.core.Response;
import java.util.Set;

public interface KeycloakService {
    Response register(UserRepresentation user);

    void assignRoles(String userId, Set<String> roles);

    AccessTokenResponse getToken(String username, String password);

    UserRepresentation getUserById(String userId);

    void changeFullName(String userId, String firstName, String lastName);

    void changePassword(String userId, String oldPassword, String newPassword);
}
