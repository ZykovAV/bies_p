package ylab.bies.ideaservice.service;

import org.springframework.transaction.annotation.Transactional;
import ylab.bies.ideaservice.dto.request.IdeaDraftRequestDto;
import ylab.bies.ideaservice.dto.response.IdeaDraftResponseDto;
import ylab.bies.ideaservice.dto.response.IdeaResponseDto;
import ylab.bies.ideaservice.entity.Idea;

import java.util.List;

@Transactional(readOnly = true)
public interface IdeaService {

    IdeaResponseDto findById(String token, Long id);

    @Transactional
    IdeaDraftResponseDto createDraftIdea(String token, IdeaDraftRequestDto request);
}
