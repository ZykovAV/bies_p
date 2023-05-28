package ylab.bies.ideaservice.service;


import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import ylab.bies.ideaservice.dto.request.IdeaDraftRequestDto;
import ylab.bies.ideaservice.dto.response.IdeaDraftResponseDto;
import ylab.bies.ideaservice.dto.response.IdeaResponseDto;


public interface IdeaService {

    IdeaResponseDto findById(String token, Long id);

    IdeaDraftResponseDto createDraftIdea(String token, IdeaDraftRequestDto request);

    Page<IdeaResponseDto> getAllIdeas(Pageable pageable);
}
