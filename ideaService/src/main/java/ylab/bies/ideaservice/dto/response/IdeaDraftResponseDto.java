package ylab.bies.ideaservice.dto.response;


import lombok.*;

/**
 * DTO for draft endpoint
 */
@Setter
@Getter
@AllArgsConstructor
public class IdeaDraftResponseDto {

    private Long id;
    private String name;
    private String text;

}
