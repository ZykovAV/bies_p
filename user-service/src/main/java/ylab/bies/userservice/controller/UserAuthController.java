package ylab.bies.userservice.controller;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.keycloak.representations.AccessTokenResponse;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import ylab.bies.userservice.dto.LoginRequest;
import ylab.bies.userservice.dto.RegisterRequest;
import ylab.bies.userservice.dto.UserResponse;
import ylab.bies.userservice.service.UserService;

import javax.validation.Valid;

@RestController
@RequestMapping(value = "/api/v1/users")
@RequiredArgsConstructor
@Slf4j
public class UserAuthController {
    private final UserService service;

    @PostMapping("/register")
    @PreAuthorize("isAnonymous()")
    public ResponseEntity<UserResponse> register(@RequestBody @Valid RegisterRequest request) {
        log.info("Registering a new user: {}", request);
        UserResponse userResponse = service.register(request);
        log.info("User registered successfully: {}", userResponse);
        return ResponseEntity.status(HttpStatus.CREATED).body(userResponse);
    }

    @PostMapping("/login")
    @PreAuthorize("isAnonymous()")
    public ResponseEntity<AccessTokenResponse> login(@RequestBody @Valid LoginRequest request) {
        log.info("Logining a user: \"{}\"", request.getUsername());
        AccessTokenResponse tokenResponse = service.login(request);
        log.info("User: \"{}\" logged successfully", request.getUsername());
        return ResponseEntity.status(HttpStatus.OK).body(tokenResponse);
    }
}
