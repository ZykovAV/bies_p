package ylab.bies.userservice.service.impl;

import lombok.RequiredArgsConstructor;
import org.keycloak.admin.client.resource.RealmResource;
import org.keycloak.representations.AccessTokenResponse;
import org.keycloak.representations.idm.RoleRepresentation;
import org.keycloak.representations.idm.UserRepresentation;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import ylab.bies.userservice.service.KeycloakService;

import javax.ws.rs.core.Response;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

@Service
@RequiredArgsConstructor
public class KeycloakServiceImpl implements KeycloakService {
    @Value("${keycloak.realm}")
    private String realm;
    @Value("${keycloak.client-id}")
    private String clientId;
    @Value("${keycloak.client-secret}")
    private String clientSecret;
    @Value("${keycloak.server-url}")
    private String serverUrl;
    private final RealmResource realmResource;

    @Override
    public Response register(UserRepresentation user) {
        return realmResource.users().create(user);
    }

    @Override
    public void assignRoles(String userId, Set<String> roles) {
        List<RoleRepresentation> userRoles = new ArrayList<>();
        for (String userRole : roles) {
            userRoles.add(realmResource.roles().get(userRole).toRepresentation());
        }
        realmResource.users().get(userId).roles().realmLevel().add(userRoles);
    }

    @Override
    public AccessTokenResponse getToken(String username, String password) {
        return null;
    }
}
