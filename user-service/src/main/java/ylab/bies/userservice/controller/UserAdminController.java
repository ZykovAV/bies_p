package ylab.bies.userservice.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import ylab.bies.userservice.service.UserService;

@RestController
@RequestMapping(value = "/api/v1/admin/users")
@RequiredArgsConstructor
@Slf4j
public class UserAdminController {
    private final UserService service;

    @PatchMapping("/{id}/roles/{roleName}")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(security = @SecurityRequirement(name = "Bearer Token"))
    public ResponseEntity<Void> assignRole(@PathVariable String id, @PathVariable String roleName) {
        log.info("Assigning role: {} to user with id: {}", roleName, id);
        service.assignRole(id, roleName.toUpperCase());
        log.info("Successfully assigned role: {} to user with id: {}", roleName, id);
        return ResponseEntity.ok().build();
    }
}
