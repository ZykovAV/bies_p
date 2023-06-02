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
import ylab.bies.userservice.exception.*;
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
    private static final String INVALID_CREDENTIALS_MESSAGE = "Invalid login or password";
    private static final String USER_ALREADY_EXISTS_MESSAGE = "User with that username or email is already exists";
    private static final String INVALID_OLD_PASSWORD_MESSAGE = "Invalid old password";
    private static final String USER_NOT_FOUND_MESSAGE = "User with ID %s not found";
    private static final String USER_NOT_FOUND_LOG_MESSAGE = "Failed to get a user's contact. User with ID {} not found";
    private static final String FAILED_TO_LOGIN_MESSAGE = "Failed to login a user";
    private static final String FAILED_TO_REGISTER_MESSAGE = "Failed to register a new User";
    private static final String FAILED_CHANGE_PASSWORD_MESSAGE = "Failed to change password";
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
            log.info("{}. {}", FAILED_TO_LOGIN_MESSAGE, INVALID_CREDENTIALS_MESSAGE);
            throw new InvalidCredentialsException(INVALID_CREDENTIALS_MESSAGE);
        }
    }

    @Override
    @Transactional(readOnly = true)
    @SuppressWarnings("OptionalGetWithoutIsPresent")
    public UserResponse getProfile() {
        UUID userId = tokenManager.getUserIdFromToken();

        User user = repository.findById(userId).get();
        UserRepresentation keycloakUser = keycloakService.getUserById(String.valueOf(userId));

        return mapper.toUserResponse(user, keycloakUser);
    }

    @Override
    @Transactional
    @SuppressWarnings("OptionalGetWithoutIsPresent")
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
        UUID userId = tokenManager.getUserIdFromToken();
        String username = tokenManager.getUsernameFromToken();
        try {
            keycloakService.getToken(username, request.getOldPassword());
        } catch (NotAuthorizedException e) {
            log.info("{}. {}", FAILED_CHANGE_PASSWORD_MESSAGE, INVALID_OLD_PASSWORD_MESSAGE);
            throw new InvalidOldPasswordException(INVALID_OLD_PASSWORD_MESSAGE);
        }
        keycloakService.changePassword(String.valueOf(userId), request.getNewPassword());
    }

    @Override
    @Transactional(readOnly = true)
    public ContactsResponse getContactsById(String id) {
        try {
            UUID userId = UUID.fromString(id);
            User user = repository.findById(userId).orElseThrow(() -> getUserNotFoundException(id));
            return mapper.toContactsResponse(user);
        } catch (IllegalArgumentException e) {
            throw getUserNotFoundException(id);
        }
    }

    private UserNotFoundException getUserNotFoundException(String id) {
        log.info(USER_NOT_FOUND_LOG_MESSAGE, id);
        return new UserNotFoundException(String.format(USER_NOT_FOUND_MESSAGE, id));
    }

    private void handleRegistrationResponse(Response response) {
        if (response.getStatusInfo() == Response.Status.CONFLICT) {
            log.info("{}. {}", FAILED_TO_REGISTER_MESSAGE, USER_ALREADY_EXISTS_MESSAGE);
            throw new UserAlreadyExistException(USER_ALREADY_EXISTS_MESSAGE);
        }
        if (response.getStatusInfo() != Response.Status.CREATED) {
            log.info(FAILED_TO_REGISTER_MESSAGE);
            throw new UserRegistrationException(FAILED_TO_REGISTER_MESSAGE);
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
