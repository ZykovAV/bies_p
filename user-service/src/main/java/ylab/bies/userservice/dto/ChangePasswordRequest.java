package ylab.bies.userservice.dto;

import lombok.Getter;
import lombok.Setter;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.Size;

@Getter
@Setter
public class ChangePasswordRequest {
    private String oldPassword;
    @NotBlank(message = "Password can't be null or empty")
    @Size(max = 32, message = "Password can't be more than {max} characters")
    private String newPassword;
}
