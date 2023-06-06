package ylab.bies.ideaservice.service;


import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import ylab.bies.ideaservice.dto.request.IdeaDraftRequestDto;
import ylab.bies.ideaservice.dto.request.IdeaRequestDto;
import ylab.bies.ideaservice.dto.response.IdeaDraftResponseDto;
import ylab.bies.ideaservice.dto.response.IdeaResponseDto;


public interface IdeaService {

    IdeaResponseDto findById(Long id);

    IdeaDraftResponseDto createDraftIdea(IdeaDraftRequestDto request);

    void changeStatus(Long id, Integer status);

    void rate(Long id, boolean isLike);

    Page<IdeaResponseDto> getAllIdeas(Pageable pageable);

    Page<IdeaResponseDto> getAllUsersIdeas(Pageable pageable);

    IdeaResponseDto updateIdea(Long id, IdeaRequestDto editRequest);
}
