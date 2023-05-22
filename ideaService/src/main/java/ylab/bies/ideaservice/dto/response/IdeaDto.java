package ylab.bies.ideaservice.dto.response;

import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
public class IdeaDto {

    private Long id;
    private String name;
    private String text;
    private Long statusId;
    private Integer rating;

}
