package ylab.bies.userservice.dto;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class ChangeNameResponse {
    private String firstName;
    private String lastName;
    private String middleName;
}
