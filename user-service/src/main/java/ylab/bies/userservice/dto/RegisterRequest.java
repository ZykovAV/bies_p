package ylab.bies.userservice.dto;

import io.smallrye.common.constraint.Nullable;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

import javax.validation.constraints.Email;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;

@Getter
@Setter
@ToString
public class RegisterRequest {
    @NotBlank
    @Size(max = 32)
    private String username;
    @NotBlank
    @Size(max = 32)
    @ToString.Exclude
    private String password;
    @NotBlank
    @Size(max = 32)
    @Email
    private String email;
    @NotBlank
    @Size(max = 32)
    private String firstName;
    @NotNull
    @NotBlank
    @Size(max = 32)
    private String lastName;
    @Nullable
    @Size(max = 32)
    private String middleName;
}
