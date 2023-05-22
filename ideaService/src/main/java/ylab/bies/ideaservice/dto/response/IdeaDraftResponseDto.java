package ylab.bies.ideaservice.dto.response;


import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class IdeaDraftResponseDto {

    private Long id;
    private String name;
    private String text;

}
