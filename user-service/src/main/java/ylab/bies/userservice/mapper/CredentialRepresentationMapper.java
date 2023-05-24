package ylab.bies.userservice.mapper;

import org.keycloak.OAuth2Constants;
import org.keycloak.representations.idm.CredentialRepresentation;
import org.springframework.stereotype.Component;

import java.util.Collections;
import java.util.List;

@Component
public class CredentialRepresentationMapper {
    public List<CredentialRepresentation> toCredentialRepresentation(String password) {
        CredentialRepresentation credential = new CredentialRepresentation();
        credential.setType(OAuth2Constants.PASSWORD);
        credential.setValue(password);
        credential.setTemporary(false);
        return Collections.singletonList(credential);
    }
}
