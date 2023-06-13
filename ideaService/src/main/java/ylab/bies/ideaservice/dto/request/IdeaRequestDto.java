package ylab.bies.ideaservice.dto.request;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.*;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.Size;

/**
 * DTO for edit and publish an idea
 */
@Setter
@Getter
public class IdeaRequestDto {

    @JsonProperty("name")
    @NotBlank(message = "name is required field")
    private String name;

    @JsonProperty("text")
    @Size(min = 10, max = 2000, message = "text should be between 10 and 2000 characters")
    private String text;

}
