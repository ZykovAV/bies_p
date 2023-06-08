package ylab.bies.ideaservice.dto.response;


import lombok.*;

@Setter
@Getter
@AllArgsConstructor
public class IdeaDraftResponseDto {

    private Long id;
    private String name;
    private String text;

}
