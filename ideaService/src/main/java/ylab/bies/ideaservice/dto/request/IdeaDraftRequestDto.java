package ylab.bies.ideaservice.dto.request;

import lombok.*;

import javax.validation.constraints.NotNull;

@Setter
@Getter
public class IdeaDraftRequestDto {

    @NotNull
    private String name;

    @NotNull
    private String text;

}
