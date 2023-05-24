package ylab.bies.userservice.dto;

import io.smallrye.common.constraint.Nullable;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.Size;

@Getter
@Setter
@ToString
public class ChangeNameRequest {
    @NotBlank(message = "First name can't be null or empty")
    @Size(max = 32, message = "First name can't be more than {max} characters")
    private String firstName;

    @NotBlank(message = "Last name can't be null or empty")
    @Size(max = 32, message = "Last name can't be more than {max} characters")
    @Size(max = 32)
    private String lastName;

    @Nullable
    @Size(max = 32, message = "Middle name can't be more than {max} characters")
    private String middleName;
}
