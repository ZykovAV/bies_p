package ylab.bies.userservice.controller;

import lombok.experimental.UtilityClass;
import org.keycloak.representations.AccessTokenResponse;
import ylab.bies.userservice.dto.*;

import java.util.Collections;
import java.util.Set;
import java.util.UUID;

@UtilityClass
public class UserTestUtil {

    public static RegisterRequest getValidRegisterRequest() {
        RegisterRequest request = new RegisterRequest();
        request.setUsername("testUsername");
        request.setEmail("test@mail.ru");
        request.setPassword("testPassword");
        request.setFirstName("testFirstName");
        request.setLastName("testLastName");
        request.setMiddleName("testMiddleName");
        return request;
    }

    public static LoginRequest getValidLoginRequest() {
        LoginRequest loginRequest = new LoginRequest();
        loginRequest.setUsername("username");
        loginRequest.setPassword("password");
        return loginRequest;
    }

    public static ChangeFullNameRequest getValidChangeFullNameRequest() {
        ChangeFullNameRequest changeFullNameRequest = new ChangeFullNameRequest();
        changeFullNameRequest.setFirstName("firstName");
        changeFullNameRequest.setLastName("lastName");
        changeFullNameRequest.setMiddleName("middleName");
        return changeFullNameRequest;
    }

    public static AccessTokenResponse getAccessTokenResponse() {
        AccessTokenResponse tokenResponse = new AccessTokenResponse();
        tokenResponse.setToken("token");
        tokenResponse.setRefreshToken("refreshToken");
        tokenResponse.setTokenType("Bearer");
        tokenResponse.setSessionState(String.valueOf(UUID.randomUUID()));
        tokenResponse.setScope("profile email");
        return tokenResponse;
    }

    public static boolean isUserResponseValid(RegisterRequest request, UserResponse response, UUID userId) {
        return response.getId().equals(userId)
                && response.getUsername().equals(request.getUsername())
                && response.getEmail().equals(request.getEmail())
                && response.getFirstName().equals(request.getFirstName())
                && response.getLastName().equals(request.getLastName())
                && response.getMiddleName().equals(request.getMiddleName())
                && response.getRoles().equals(getDefaultRoleResponeSet());
    }

    public static boolean isChangeFullNameResponseValid(ChangeFullNameRequest request, ChangeFullNameResponse response) {
        return response.getFirstName().equals(request.getFirstName())
                && response.getLastName().equals(request.getLastName())
                && response.getMiddleName().equals(request.getMiddleName());
    }

    private Set<RoleResponse> getDefaultRoleResponeSet() {
        RoleResponse role = new RoleResponse();
        role.setName("USER");
        return Collections.singleton(role);
    }
}
