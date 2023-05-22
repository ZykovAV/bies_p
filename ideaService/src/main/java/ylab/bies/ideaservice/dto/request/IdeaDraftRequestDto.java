package ylab.bies.ideaservice.dto.request;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.validation.constraints.NotNull;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class IdeaDraftRequestDto {

    @NotNull
    private String name;

    @NotNull
    private String text;
}
