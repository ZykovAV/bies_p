package ylab.bies.userservice.dto;

import io.smallrye.common.constraint.Nullable;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.Pattern;
import javax.validation.constraints.Size;

@Getter
@Setter
@ToString
public class ChangeFullNameRequest {
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
