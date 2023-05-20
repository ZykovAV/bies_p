package ylab.bies.userservice.controller;

import lombok.experimental.UtilityClass;
import ylab.bies.userservice.dto.RegisterRequest;
import ylab.bies.userservice.dto.RoleResponse;
import ylab.bies.userservice.dto.UserResponse;

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

    public RegisterRequest getInvalidRegisterRequest() {
        RegisterRequest request = new RegisterRequest();
        request.setUsername("sa");
        request.setEmail("invalidEmail");
        request.setPassword("sa");
        request.setFirstName("testFirstName");
        request.setLastName("testLastName");
        request.setMiddleName("testMiddleName");
        return request;
    }

    public static boolean isUserResponseValid(RegisterRequest request, UserResponse response, UUID userId) {
        return response.getId().equals(userId)
                && response.getEmail().equals(request.getEmail())
                && response.getFirstName().equals(request.getFirstName())
                && response.getLastName().equals(request.getLastName())
                && response.getMiddleName().equals(request.getMiddleName())
                && response.getRoles().equals(getDefaultRoleResponeSet());
    }

    private Set<RoleResponse> getDefaultRoleResponeSet() {
        RoleResponse role = new RoleResponse();
        role.setName("USER");
        return Collections.singleton(role);
    }
}
