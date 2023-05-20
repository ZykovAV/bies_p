package ylab.bies.userservice.dto;

import lombok.Getter;
import lombok.Setter;

import javax.validation.constraints.Size;

@Getter
@Setter
public class ChangePasswordRequest {
    private String oldPassword;
    @Size(max = 32)
    private String newPassword;
}
