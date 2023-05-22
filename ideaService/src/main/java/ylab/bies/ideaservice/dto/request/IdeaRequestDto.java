package ylab.bies.ideaservice.dto.request;


import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class IdeaRequestDto {

    private Long id;
    private String name;
    private String text;

}
