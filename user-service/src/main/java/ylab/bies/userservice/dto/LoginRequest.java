package ylab.bies.userservice.dto;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

import javax.validation.constraints.NotBlank;

@Getter
@Setter
@ToString
public class LoginRequest {
    @NotBlank
    private String username;
    @NotBlank
    @ToString.Exclude
    private String password;
}
