package ylab.bies.ideaservice.dto.request;

import lombok.*;

import javax.validation.constraints.NotBlank;

@Setter
@Getter
public class IdeaRequestDto {

    private Long id;

    @NotBlank
    private String name;

    @NotBlank
    private String text;

}
