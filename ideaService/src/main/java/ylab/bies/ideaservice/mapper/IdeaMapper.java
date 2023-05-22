package ylab.bies.ideaservice.mapper;

import org.mapstruct.Mapper;
import ylab.bies.ideaservice.dto.request.IdeaDraftRequestDto;
import ylab.bies.ideaservice.dto.response.IdeaDraftResponseDto;
import ylab.bies.ideaservice.dto.response.IdeaResponseDto;
import ylab.bies.ideaservice.entity.Idea;

@Mapper(componentModel = "spring")
public interface IdeaMapper {

    Idea ideaDraftRequestDtoToIdeaEntity(IdeaDraftRequestDto requestDto);
    IdeaDraftResponseDto ideaEntityToIdeaDraftResponseDto(Idea idea);
    IdeaResponseDto ideaEntityToIdeaResponseDto(Idea idea);

}
