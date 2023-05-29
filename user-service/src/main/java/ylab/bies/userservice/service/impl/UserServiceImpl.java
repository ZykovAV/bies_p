package ylab.bies.userservice.service.impl;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.keycloak.admin.client.CreatedResponseUtil;
import org.keycloak.representations.AccessTokenResponse;
import org.keycloak.representations.idm.UserRepresentation;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ylab.bies.userservice.config.ApplicationConfiguration;
import ylab.bies.userservice.dto.*;
import ylab.bies.userservice.entity.Role;
import ylab.bies.userservice.entity.User;
import ylab.bies.userservice.exception.InvalidCredentialsException;
import ylab.bies.userservice.exception.UserAlreadyExistException;
import ylab.bies.userservice.exception.UserRegistrationException;
import ylab.bies.userservice.mapper.UserMapper;
import ylab.bies.userservice.repository.RoleRepository;
import ylab.bies.userservice.repository.UserRepository;
import ylab.bies.userservice.service.KeycloakService;
import ylab.bies.userservice.service.UserService;
import ylab.bies.userservice.util.AccessTokenManager;

import javax.ws.rs.NotAuthorizedException;
import javax.ws.rs.core.Response;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
public class UserServiceImpl implements UserService {
    private final ApplicationConfiguration configuration;
    private final UserRepository repository;
    private final RoleRepository roleRepository;
    private final KeycloakService keycloakService;
    private final UserMapper mapper;
    private final AccessTokenManager tokenManager;

    @Override
    @Transactional
    public UserResponse register(RegisterRequest request) {
        UserRepresentation keycloakUser = mapper.toUserRepresentation(request);
        try (Response keycloakResponse = keycloakService.register(keycloakUser)) {
            handleRegistrationResponse(keycloakResponse);

            UUID userId = getUserIdFromResponse(keycloakResponse);

            keycloakService.assignRoles(String.valueOf(userId), configuration.getUserDefaultRoles());

            User user = mapper.toUser(request);
            user = create(user, userId);

            return mapper.toUserResponse(user, keycloakUser);
        }
    }

    @Override
    public AccessTokenResponse login(LoginRequest request) {
        try {
            return keycloakService.getToken(request.getUsername(), request.getPassword());
        } catch (NotAuthorizedException e) {
            log.info("Failed to login a user. Invalid login or password");
            throw new InvalidCredentialsException("Invalid login or password");
        }
    }

    @Override
    public UserResponse getProfile() {
        UUID userId = tokenManager.getUserIdFromToken();

        User user = repository.findById(userId).get();
        UserRepresentation keycloakUser = keycloakService.getUserById(String.valueOf(userId));

        return mapper.toUserResponse(user, keycloakUser);
    }

    @Override
    public ChangeFullNameResponse changeFullName(ChangeFullNameRequest request) {
        UUID userId = tokenManager.getUserIdFromToken();

        keycloakService.changeFullName(String.valueOf(userId), request.getFirstName(), request.getLastName());

        User user = repository.findById(userId).get();
        user.setFirstName(request.getFirstName());
        user.setLastName(request.getLastName());
        user.setMiddleName(request.getMiddleName());
        repository.save(user);
        return mapper.toChangeFullNameResponse(user);
    }

    @Override
    public void changePassword(ChangePasswordRequest request) {

    }

    private void handleRegistrationResponse(Response response) {
        if (response.getStatusInfo() == Response.Status.CONFLICT) {
            log.info("Failed to register a new User. User with that username or email is already exists");
            throw new UserAlreadyExistException("User with that username or email is already exists");
        }
        if (response.getStatusInfo() != Response.Status.CREATED) {
            log.info("Failed to register a new User.");
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
        for (String userRole : configuration.getUserDefaultRoles()) {
            Role role = roleRepository.findByName(userRole);
            user.getRoles().add(role);
        }
    }
}
