package ylab.bies.userservice.service;

import org.keycloak.representations.AccessTokenResponse;
import ylab.bies.userservice.dto.*;

public interface UserService {
    UserResponse register(RegisterRequest request);

    AccessTokenResponse login(LoginRequest request);

    UserResponse getProfile();

    ChangeFullNameResponse changeFullName(ChangeFullNameRequest request);

    void changePassword(ChangePasswordRequest request);

    ContactsResponse getContactsById(String id);

    ContactsPagination getAllContacts(int page);
}
