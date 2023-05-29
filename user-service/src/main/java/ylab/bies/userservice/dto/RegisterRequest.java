package ylab.bies.userservice.dto;

import io.smallrye.common.constraint.Nullable;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

import javax.validation.constraints.Email;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.Pattern;
import javax.validation.constraints.Size;

@Getter
@Setter
@ToString
public class RegisterRequest {
    @NotBlank(message = "Username can't be null or empty")
    @Size(max = 32, message = "Username can't be more than {max} characters")
    private String username;

    @NotBlank(message = "Password can't be null or empty")
    @Size(max = 32, message = "Password can't be more than {max} characters")
    @ToString.Exclude
    private String password;

    @NotBlank(message = "Email can't be null or empty")
    @Size(max = 32, message = "Email can't be more than {max} characters")
    @Email(message = "Email must have valid format")
    private String email;

    @NotBlank(message = "First name can't be null or empty")
    @Size(max = 32, message = "First name can't be more than {max} characters")
    @Pattern(regexp = "\\p{L}+", message = "First name can't contains any non-alphabetic characters")
    private String firstName;

    @NotBlank(message = "Last name can't be null or empty")
    @Size(max = 32, message = "Last name can't be more than {max} characters")
    @Pattern(regexp = "\\p{L}+", message = "Last name can't contains any non-alphabetic characters")
    private String lastName;

    @Nullable
    @Size(max = 32, message = "Middle name can't be more than {max} characters")
    @Pattern(regexp = "\\p{L}+", message = "Middle name can't contains any non-alphabetic characters")
    private String middleName;
}
