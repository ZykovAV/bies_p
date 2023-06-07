package ylab.bies.userservice.service.impl;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.keycloak.admin.client.CreatedResponseUtil;
import org.keycloak.representations.AccessTokenResponse;
import org.keycloak.representations.idm.UserRepresentation;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.mapping.PropertyReferenceException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ylab.bies.userservice.config.ApplicationConfiguration;
import ylab.bies.userservice.dto.*;
import ylab.bies.userservice.entity.Role;
import ylab.bies.userservice.entity.User;
import ylab.bies.userservice.exception.*;
import ylab.bies.userservice.mapper.UserMapper;
import ylab.bies.userservice.projection.UserProjection;
import ylab.bies.userservice.repository.RoleRepository;
import ylab.bies.userservice.repository.UserRepository;
import ylab.bies.userservice.service.KeycloakService;
import ylab.bies.userservice.service.UserService;
import ylab.bies.userservice.util.AccessTokenManager;

import javax.ws.rs.NotAuthorizedException;
import javax.ws.rs.core.Response;
import java.util.Collections;
import java.util.UUID;

import static java.lang.String.format;

@Service
@RequiredArgsConstructor
@Slf4j
public class UserServiceImpl implements UserService {
    private static final String INVALID_CREDENTIALS_MESSAGE = "Invalid login or password";
    private static final String USER_ALREADY_EXISTS_MESSAGE = "User with that username or email is already exists";
    private static final String INVALID_OLD_PASSWORD_MESSAGE = "Invalid old password";
    private static final String USER_NOT_FOUND_MESSAGE = "User with ID %s not found";
    private static final String USER_ALREADY_HAS_ROLE_MESSAGE = "User with ID: %s already has role: %s";
    private static final String ROLE_NOT_FOUND_MESSAGE = "Role with name: %s not found";
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
            user = prepareAndSave(user, userId);

            return mapper.toUserResponse(user, keycloakUser);
        }
    }

    @Override
    public AccessTokenResponse login(LoginRequest request) {
        try {
            return keycloakService.getToken(request.getUsername(), request.getPassword());
        } catch (NotAuthorizedException e) {
            throw logAndGetException(new InvalidCredentialsException(INVALID_CREDENTIALS_MESSAGE));
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
        String username = tokenManager.getUsernameFromToken();
        try {
            keycloakService.getToken(username, request.getOldPassword());
            UUID userId = tokenManager.getUserIdFromToken();
            keycloakService.changePassword(String.valueOf(userId), request.getNewPassword());
        } catch (NotAuthorizedException e) {
            throw logAndGetException(new InvalidOldPasswordException(INVALID_OLD_PASSWORD_MESSAGE));
        }
    }

    @Override
    @Transactional(readOnly = true)
    public ContactsResponse getContactsById(String id) {
        try {
            UUID userId = UUID.fromString(id);
            UserProjection user = repository.findProjectedById(userId).orElseThrow(() ->
                    logAndGetException(new UserNotFoundException(format(USER_NOT_FOUND_MESSAGE, id)))
            );
            return mapper.toContactsResponse(user);
        } catch (IllegalArgumentException e) {
            throw logAndGetException(new UserNotFoundException(format(USER_NOT_FOUND_MESSAGE, id)));
        }
    }

    @Override
    @Transactional(readOnly = true)
    public ContactsPageResponse getAllContacts(Pageable pageable) {
        try {
            Page<UserProjection> userPage = repository.findAllProjectedBy(pageable);
            return mapper.toContactsPageResponse(userPage);
        } catch (PropertyReferenceException e) {
            throw logAndGetException(new InvalidSortPropertyException(e.getMessage()));
        }
    }

    @Override
    @Transactional
    public void assignRole(String id, String roleName) {
        UUID userId;
        try {
            userId = UUID.fromString(id);
        } catch (IllegalArgumentException e) {
            throw logAndGetException(new UserNotFoundException(format(USER_NOT_FOUND_MESSAGE, id)));
        }

        User user = getUserById(userId);

        if (isUserHasRole(user, roleName)) {
            throw logAndGetException(
                    new UserAlreadyHasRoleException(format(USER_ALREADY_HAS_ROLE_MESSAGE, id, roleName))
            );
        }

        Role role = getRoleByName(roleName);
        user.getRoles().add(role);
        keycloakService.assignRoles(id, Collections.singleton(roleName));

        repository.save(user);
    }

    private RuntimeException logAndGetException(RuntimeException exception) {
        log.info("Request failed: {}", exception.getMessage());
        return exception;
    }

    private void handleRegistrationResponse(Response response) {
        if (response.getStatusInfo() == Response.Status.CONFLICT) {
            throw logAndGetException(new UserAlreadyExistException(USER_ALREADY_EXISTS_MESSAGE));
        }
    }

    private UUID getUserIdFromResponse(Response response) {
        return UUID.fromString(CreatedResponseUtil.getCreatedId(response));
    }

    private User prepareAndSave(User user, UUID userId) {
        user.setId(userId);
        assignDefaultRoles(user);
        return repository.save(user);
    }

    private void assignDefaultRoles(User user) {
        for (String userRole : configuration.getUserDefaultRoles()) {
            Role role = getRoleByName(userRole);
            user.getRoles().add(role);
        }
    }

    private Role getRoleByName(String name) {
        return roleRepository.findByName(name).orElseThrow(() ->
                logAndGetException(new RoleNotFoundException(format(ROLE_NOT_FOUND_MESSAGE, name)))
        );
    }

    private User getUserById(UUID id) {
        return repository.findById(id).orElseThrow(() ->
                logAndGetException(new UserNotFoundException(format(USER_NOT_FOUND_MESSAGE, id)))
        );
    }

    private boolean isUserHasRole(User user, String roleName) {
        return user.getRoles()
                .stream()
                .map(Role::getName)
                .anyMatch(name -> name.equals(roleName));
    }
}
