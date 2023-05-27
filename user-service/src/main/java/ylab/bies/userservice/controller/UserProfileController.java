package ylab.bies.userservice.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import ylab.bies.userservice.dto.UserResponse;
import ylab.bies.userservice.service.UserService;

@RestController
@RequestMapping(value = "/api/v1/users/profile")
@RequiredArgsConstructor
@Slf4j
public class UserProfileController {
    private final UserService service;

    @GetMapping
    @Operation(security = @SecurityRequirement(name = "Bearer Token"))
    public ResponseEntity<UserResponse> getProfile() {
        log.info("Getting a user profile from token");
        UserResponse userResponse = service.getProfile();
        log.info("User: \"{}\" has successfully received a profile", userResponse.getUsername());
        return ResponseEntity.status(HttpStatus.OK).body(userResponse);
    }
}
