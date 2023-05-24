package ylab.bies.ideaservice.dto.response;

import lombok.*;

@Setter
@Getter
public class IdeaDto {

    private Long id;
    private String name;
    private String text;
    private Integer status;
    private Integer rating;

}
