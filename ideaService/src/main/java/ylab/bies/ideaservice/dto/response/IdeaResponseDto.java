package ylab.bies.ideaservice.dto.response;


import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class IdeaResponseDto {

    private Long id;
    private String userId;
    private String name;
    private String text;
    private Long statusId;
    private Integer rating;
    private Boolean userLike;
    private Boolean userDislike;

}
