package ylab.bies.ideaservice.service;

import ylab.bies.ideaservice.dto.request.IdeaDraftRequestDto;
import ylab.bies.ideaservice.dto.response.IdeaDraftResponseDto;

public interface IdeaService {

    IdeaDraftResponseDto createDraftIdea(String token, IdeaDraftRequestDto request);
}
