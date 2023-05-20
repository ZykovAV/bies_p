package ylab.bies.userservice.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

import javax.validation.constraints.NotBlank;

@Getter
@Setter
@ToString
@Schema(description = "DTO для регистрации пользователя")
public class LoginRequest {
    @NotBlank
    private String username;
    @NotBlank
    @ToString.Exclude
    private String password;
}
