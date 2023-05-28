package ylab.bies.ideaservice.service.impl;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ylab.bies.ideaservice.dto.request.IdeaDraftRequestDto;
import ylab.bies.ideaservice.dto.response.IdeaDraftResponseDto;
import ylab.bies.ideaservice.dto.response.IdeaResponseDto;
import ylab.bies.ideaservice.entity.Idea;
import ylab.bies.ideaservice.exception.IdeaNotFoundException;
import ylab.bies.ideaservice.mapper.IdeaMapper;
import ylab.bies.ideaservice.repository.IdeaRepository;
import ylab.bies.ideaservice.service.IdeaService;
import ylab.bies.ideaservice.service.VoteService;
import ylab.bies.ideaservice.util.AccessTokenDecoder;

import java.util.UUID;


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
    public Page<IdeaResponseDto> getAllIdeas(Pageable pageable) {
        Page<Idea> ideas = ideaRepository.findAllByStatusNotOrderByRatingDesc(1, pageable);
        if (ideas == null) {
            log.error("List of ideas not received ");
            throw new IdeaNotFoundException("List of ideas not received ");
        }
        log.info("List all ideas: {}", ideas);
        Page<IdeaResponseDto> listDto = ideas.map(ideaMapper::ideaEntityToIdeaResponseDto);
        listDto.forEach(responseDto -> responseDto.setUserLike(voteService.getVoteOfUser(responseDto.getUserId(), responseDto.getId())));
        return listDto;
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
