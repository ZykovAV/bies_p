package ylab.bies.userservice.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import ylab.bies.userservice.dto.ChangeFullNameRequest;
import ylab.bies.userservice.dto.ChangeFullNameResponse;
import ylab.bies.userservice.dto.UserResponse;
import ylab.bies.userservice.service.UserService;

import javax.validation.Valid;

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

    @PutMapping("/fullName")
    @Operation(security = @SecurityRequirement(name = "Bearer Token"))
    public ResponseEntity<ChangeFullNameResponse> changeName(@RequestBody @Valid ChangeFullNameRequest request) {
        log.info("Changing user's full name by request {}", request);
        ChangeFullNameResponse response = service.changeFullName(request);
        log.info("User successfully changed his full name");
        return ResponseEntity.status(HttpStatus.OK).body(response);
    }
}
