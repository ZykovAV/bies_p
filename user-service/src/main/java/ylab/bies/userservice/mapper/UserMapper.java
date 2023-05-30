package ylab.bies.userservice.mapper;

import org.keycloak.representations.idm.UserRepresentation;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import ylab.bies.userservice.dto.ChangeFullNameResponse;
import ylab.bies.userservice.dto.RegisterRequest;
import ylab.bies.userservice.dto.UserResponse;
import ylab.bies.userservice.entity.User;

@Mapper(componentModel = "spring", uses = CredentialRepresentationMapper.class)
public interface UserMapper {
    @Mapping(source = "user.id", target = "id")
    @Mapping(source = "keycloakUser.username", target = "username")
    @Mapping(source = "user.email", target = "email")
    @Mapping(source = "user.firstName", target = "firstName")
    @Mapping(source = "user.lastName", target = "lastName")
    UserResponse toUserResponse(User user, UserRepresentation keycloakUser);

    User toUser(RegisterRequest request);

    @Mapping(source = "password", target = "credentials")
    @Mapping(target = "enabled", constant = "true")
    @Mapping(target = "emailVerified", constant = "true")
    UserRepresentation toUserRepresentation(RegisterRequest request);

    ChangeFullNameResponse toChangeFullNameResponse(User user);
}
