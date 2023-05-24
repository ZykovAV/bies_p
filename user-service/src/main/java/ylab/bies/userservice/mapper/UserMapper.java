package ylab.bies.userservice.mapper;

import org.keycloak.representations.idm.UserRepresentation;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import ylab.bies.userservice.dto.RegisterRequest;
import ylab.bies.userservice.dto.UserResponse;
import ylab.bies.userservice.entity.User;

@Mapper(componentModel = "spring", uses = CredentialRepresentationMapper.class)
public interface UserMapper {
    UserResponse toUserRepose(User user);

    User toUser(RegisterRequest request);

    @Mapping(source = "password", target = "credentials")
    @Mapping(target = "enabled", constant = "true")
    @Mapping(target = "emailVerified", constant = "true")
    UserRepresentation toUserRepresentation(RegisterRequest request);
}
