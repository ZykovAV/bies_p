package ylab.bies.ideaservice.service;


import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import ylab.bies.ideaservice.dto.request.IdeaDraftRequestDto;
import ylab.bies.ideaservice.dto.response.IdeaDraftResponseDto;
import ylab.bies.ideaservice.dto.response.IdeaResponseDto;


public interface IdeaService {

    IdeaResponseDto findById(String token, Long id);

    IdeaDraftResponseDto createDraftIdea(String token, IdeaDraftRequestDto request);

    HttpStatus changeStatus(Long id, Integer status);

    Page<IdeaResponseDto> getAllIdeas(Pageable pageable);
}
