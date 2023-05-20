package ylab.bies.userservice.service.impl;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.keycloak.admin.client.CreatedResponseUtil;
import org.keycloak.representations.AccessTokenResponse;
import org.keycloak.representations.idm.UserRepresentation;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import ylab.bies.userservice.dto.*;
import ylab.bies.userservice.entity.Role;
import ylab.bies.userservice.entity.User;
import ylab.bies.userservice.exception.UserAlreadyExistException;
import ylab.bies.userservice.exception.UserRegistrationException;
import ylab.bies.userservice.mapper.UserMapper;
import ylab.bies.userservice.repository.RoleRepository;
import ylab.bies.userservice.repository.UserRepository;
import ylab.bies.userservice.service.KeycloakService;
import ylab.bies.userservice.service.UserService;

import javax.ws.rs.core.Response;
import java.util.Set;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
public class UserServiceImpl implements UserService {
    @Value("#{'${application.user-default-roles}'.split(',')}")
    private Set<String> userDefaultRoles;
    private final UserRepository repository;
    private final RoleRepository roleRepository;
    private final KeycloakService keycloakService;
    private final UserMapper mapper;

    @Override
    public UserResponse register(RegisterRequest request) {
        UserRepresentation keycloakUser = mapper.toUserRepresentation(request);
        try (Response keycloakResponse = keycloakService.register(keycloakUser)) {
            handleRegistrationResponse(keycloakResponse);
            UUID userId = getUserIdFromResponse(keycloakResponse);
            keycloakService.assignRoles(String.valueOf(userId), userDefaultRoles);
            User user = mapper.toUser(request);
            return mapper.toUserRepose(create(user, userId));
        }
    }

    @Override
    public AccessTokenResponse login(LoginRequest request) {
        return null;
    }

    @Override
    public UserResponse getProfile(String token) {
        return null;
    }

    @Override
    public ChangeNameResponse changeName(ChangeNameRequest request) {
        return null;
    }

    @Override
    public void changePassword(ChangePasswordRequest request) {

    }

    private void handleRegistrationResponse(Response response) {
        if (response.getStatusInfo() == Response.Status.CONFLICT) {
            throw new UserAlreadyExistException("User with that username or email is already exists");
        }
        if (response.getStatusInfo() != Response.Status.CREATED) {
            throw new UserRegistrationException("Failed to register a new user");
        }
    }

    private UUID getUserIdFromResponse(Response response) {
        return UUID.fromString(CreatedResponseUtil.getCreatedId(response));
    }

    private User create(User user, UUID userId) {
        user.setId(userId);
        assignDefaultRoles(user);
        return repository.save(user);
    }

    private void assignDefaultRoles(User user) {
        for (String userRole : userDefaultRoles) {
            Role role = roleRepository.findByName(userRole);
            user.getRoles().add(role);
        }
    }
}
