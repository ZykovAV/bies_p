package ylab.bies.ideaservice.service;

import ylab.bies.ideaservice.dto.request.IdeaDraftRequestDto;
import ylab.bies.ideaservice.dto.response.IdeaDraftResponseDto;
import ylab.bies.ideaservice.dto.response.IdeaResponseDto;

import java.util.List;

public interface IdeaService {



    IdeaDraftResponseDto createDraftIdea(String token, IdeaDraftRequestDto request);
}
