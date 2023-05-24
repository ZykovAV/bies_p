package ylab.bies.ideaservice.dto.response;


import lombok.*;

import java.util.UUID;

@Setter
@Getter
@RequiredArgsConstructor
public class IdeaResponseDto {

    private final Long id;
    private final String name;
    private final String text;
    private final Integer rating;
    private final String userId;
    private final Integer status;
    private final Boolean userLike;


}
