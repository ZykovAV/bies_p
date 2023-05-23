package ylab.bies.ideaservice.dto.response;

import lombok.*;

@Setter
@Getter
@RequiredArgsConstructor
public class IdeaDto {

    private Long id;
    private String name;
    private String text;
    private Long statusId;
    private Integer rating;

}
