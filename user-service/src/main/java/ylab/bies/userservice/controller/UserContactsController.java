package ylab.bies.userservice.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import ylab.bies.userservice.dto.ContactsResponse;
import ylab.bies.userservice.service.UserService;

@RestController
@RequestMapping(value = "/api/v1/users")
@RequiredArgsConstructor
@Slf4j
public class UserContactsController {
    private final UserService service;

    @GetMapping("/{id}/contacts")
    @Operation(security = @SecurityRequirement(name = "Bearer Token"))
    public ResponseEntity<ContactsResponse> getUserContactById(@PathVariable String id) {
        log.info("Getting a user's contacts by id: {}", id);
        ContactsResponse contactsResponse = service.getContactsById(id);
        log.info("Successfully received a user's contacts: {}", contactsResponse);
        return ResponseEntity.status(HttpStatus.OK).body(contactsResponse);
    }
}
