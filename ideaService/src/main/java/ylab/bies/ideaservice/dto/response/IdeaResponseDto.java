package ylab.bies.ideaservice.dto.response;


import lombok.*;

@Setter
@Getter
@RequiredArgsConstructor
public class IdeaResponseDto {

    private Long id;
    private String userId;
    private String name;
    private String text;
    private Long statusId;
    private Integer rating;
    private Boolean userLike;

}
