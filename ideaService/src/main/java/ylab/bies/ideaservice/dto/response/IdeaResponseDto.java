package ylab.bies.ideaservice.dto.response;


import lombok.*;

import java.util.UUID;

@Setter
@Getter
public class IdeaResponseDto {

    private Long id;
    private String name;
    private String text;
    private Integer rating;
    private UUID userId;
    private Integer status;
    private Boolean userLike;


}
