package ylab.bies.ideaservice.dto.request;


import lombok.*;

import javax.validation.constraints.NotNull;

@Setter
@Getter
@RequiredArgsConstructor
public class IdeaRequestDto {

    @NotNull
    private Long id;

    @NotNull
    private String name;

    @NotNull
    private String text;

}
