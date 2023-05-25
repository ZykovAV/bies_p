package ylab.bies.ideaservice.service.impl;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ylab.bies.ideaservice.dto.request.IdeaDraftRequestDto;
import ylab.bies.ideaservice.dto.response.IdeaDraftResponseDto;
import ylab.bies.ideaservice.dto.response.IdeaResponseDto;
import ylab.bies.ideaservice.entity.Idea;
import ylab.bies.ideaservice.mapper.IdeaMapper;
import ylab.bies.ideaservice.repository.IdeaRepository;
import ylab.bies.ideaservice.service.IdeaService;
import ylab.bies.ideaservice.service.VoteService;
import ylab.bies.ideaservice.util.AccessTokenDecoder;

import java.util.List;
import java.util.Objects;
import java.util.UUID;
import java.util.stream.Collectors;

import static ylab.bies.ideaservice.util.enums.Status.DRAFT;


@Slf4j
@Service
@RequiredArgsConstructor
public class IdeaServiceImpl implements IdeaService {

    private final VoteService voteService;
    private final IdeaRepository ideaRepository;
    private final AccessTokenDecoder decoder;
    private final IdeaMapper ideaMapper;


    @Transactional(readOnly = true)
    public IdeaResponseDto findById(String token, Long id) {
        UUID userId = decoder.getUuidFromToken(token);
        IdeaResponseDto response;
        Idea idea = ideaRepository.findById(id).orElse(null);
        if (idea == null) {
            response = null;
        } else {
            response = ideaMapper.ideaEntityToIdeaResponseDto(idea);
            response.setRating(voteService.getRating(id));
            response.setUserLike(voteService.getVoteOfUser(userId, id));
        }
        return response;
    }

    @Transactional(readOnly = true)
    public List<IdeaResponseDto> getAllIdeas() {
        List<Idea> ideas = ideaRepository.findAllIdeas();
        log.info("List all ideas: {}", ideas);
        return ideas.stream()
                .filter(Objects::nonNull)
                .map(ideaMapper::ideaEntityToIdeaResponseDto)
                .peek(responseDto -> responseDto.setUserLike(voteService.getVoteOfUser(responseDto.getUserId(), responseDto.getId())))
                .collect(Collectors.toList());
    }


    @Transactional
    public IdeaDraftResponseDto createDraftIdea(String token, IdeaDraftRequestDto draftRequestDto) {
        UUID userId = decoder.getUuidFromToken(token);
        Idea draft = ideaMapper.ideaDraftRequestDtoToIdeaEntity(draftRequestDto);
        draft.setUserId(userId);
        draft.setStatus(DRAFT.getValue());
        Idea savedDraft = ideaRepository.save(draft);
        log.info("Draft saved: {}", draft);
        return ideaMapper.ideaEntityToIdeaDraftResponseDto(savedDraft);
    }
}
