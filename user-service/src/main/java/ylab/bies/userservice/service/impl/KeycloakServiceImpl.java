package ylab.bies.userservice.service.impl;

import lombok.RequiredArgsConstructor;
import org.keycloak.OAuth2Constants;
import org.keycloak.admin.client.Keycloak;
import org.keycloak.admin.client.KeycloakBuilder;
import org.keycloak.admin.client.resource.RealmResource;
import org.keycloak.representations.AccessTokenResponse;
import org.keycloak.representations.idm.RoleRepresentation;
import org.keycloak.representations.idm.UserRepresentation;
import org.springframework.stereotype.Service;
import ylab.bies.userservice.config.KeycloakConfiguration;
import ylab.bies.userservice.service.KeycloakService;

import javax.ws.rs.core.Response;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

@Service
@RequiredArgsConstructor
public class KeycloakServiceImpl implements KeycloakService {
    private final KeycloakConfiguration configuration;
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
        try (Keycloak keycloak = KeycloakBuilder.builder()
                .grantType(OAuth2Constants.PASSWORD)
                .serverUrl(configuration.getServerUrl())
                .clientId(configuration.getClientId())
                .clientSecret(configuration.getClientSecret())
                .realm(configuration.getRealm())
                .username(username)
                .password(password)
                .build()) {
            return keycloak.tokenManager().getAccessToken();
        }
    }

    @Override
    public UserRepresentation getUserById(String userId) {
        return realmResource.users().get(userId).toRepresentation();
    }

    @Override
    public void changeFullName(String userId, String firstName, String lastName) {
        UserRepresentation user = getUserById(userId);

        user.setFirstName(firstName);
        user.setLastName(lastName);

        realmResource.users().get(userId).update(user);
    }
}
