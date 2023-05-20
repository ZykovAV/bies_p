package ylab.bies.userservice.dto;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

import java.util.Set;
import java.util.UUID;

@Getter
@Setter
@ToString
public class UserResponse {
    private UUID id;
    private String email;
    private String firstName;
    private String lastName;
    private String middleName;
    private Set<RoleResponse> roles;
}
