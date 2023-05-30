package ylab.bies.userservice.service;

import org.keycloak.representations.AccessTokenResponse;
import ylab.bies.userservice.dto.*;

public interface UserService {
    UserResponse register(RegisterRequest request);

    AccessTokenResponse login(LoginRequest request);

    UserResponse getProfile();

    ChangeNameResponse changeName(ChangeNameRequest request);

    void changePassword(ChangePasswordRequest request);

}
