package ylab.bies.userservice.dto;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

import javax.validation.constraints.NotBlank;

@Getter
@Setter
public class LoginRequest {
    @NotBlank(message = "Password can't be null or empty")
    private String username;
    @NotBlank(message = "Password can't be null or empty")
    @ToString.Exclude
    private String password;
}
