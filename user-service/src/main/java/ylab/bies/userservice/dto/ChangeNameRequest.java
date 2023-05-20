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
    @NotBlank
    @Size(max = 32)
    private String firstName;
    @NotBlank
    @Size(max = 32)
    private String lastName;
    @Nullable
    @Size(max = 32)
    private String middleName;
}
