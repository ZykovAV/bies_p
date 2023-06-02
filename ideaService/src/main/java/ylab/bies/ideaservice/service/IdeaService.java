package ylab.bies.ideaservice.service;


import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import ylab.bies.ideaservice.dto.request.IdeaDraftRequestDto;
import ylab.bies.ideaservice.dto.request.IdeaRequestDto;
import ylab.bies.ideaservice.dto.response.IdeaDraftResponseDto;
import ylab.bies.ideaservice.dto.response.IdeaResponseDto;


public interface IdeaService {

    IdeaResponseDto findById(String token, Long id);

    IdeaDraftResponseDto createDraftIdea(String token, IdeaDraftRequestDto request);

    void changeStatus(Long id, Integer status);

    void rate(String token, Long id, boolean isLike);

    Page<IdeaResponseDto> getAllIdeas(Pageable pageable);

    IdeaResponseDto updateIdea(String token, Long id, IdeaRequestDto editRequest);
}
