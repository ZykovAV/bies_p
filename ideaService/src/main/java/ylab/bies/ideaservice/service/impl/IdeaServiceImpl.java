package ylab.bies.ideaservice.service.impl;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ylab.bies.ideaservice.dto.request.IdeaDraftRequestDto;
import ylab.bies.ideaservice.dto.response.IdeaDraftResponseDto;
import ylab.bies.ideaservice.dto.response.IdeaResponseDto;
import ylab.bies.ideaservice.entity.Idea;
import ylab.bies.ideaservice.exception.AccessDeniedException;
import ylab.bies.ideaservice.exception.IdeaNotFoundException;
import ylab.bies.ideaservice.exception.StatusNotChangedException;
import ylab.bies.ideaservice.mapper.IdeaMapper;
import ylab.bies.ideaservice.repository.IdeaRepository;
import ylab.bies.ideaservice.service.IdeaService;
import ylab.bies.ideaservice.service.VoteService;
import ylab.bies.ideaservice.util.AccessTokenDecoder;
import ylab.bies.ideaservice.util.enums.Status;

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


    @Override
    @Transactional(readOnly = true)
    public IdeaResponseDto findById(String token, Long id) {
        Idea idea = ideaRepository.findById(id).orElse(null);
        UUID userId = decoder.getUuidFromToken(token);
        if (idea == null) {
            log.info("There's no idea with id={}", id);
            throw new IdeaNotFoundException("There's no idea with id=" + id);
        }
        // если идея - черновик и запрашивает не его автор
        if (idea.getStatus() == DRAFT.getValue() && !userId.equals(idea.getUserId())) {
            log.info("Access denied to idea with id={}", id);
            throw new AccessDeniedException("Access denied to idea with id=" + id);
        }
        IdeaResponseDto response = ideaMapper.ideaEntityToIdeaResponseDto(idea);
        response.setUserLike(voteService.getVoteOfUser(userId, id));
        log.info("Get idea with id={}", id);
        return response;
    }

    @Override
    @Transactional
    public HttpStatus changeStatus(Long id, Integer status) {
        if (status == null ||
                (status != Status.ACCEPTED.getValue() && status != Status.REJECTED.getValue())) {
            log.info("Status for idea id={} can be changed only to ACCEPTED(3) or REJECTED(4)", id);
            throw new StatusNotChangedException("Status can be changed only to ACCEPTED(3) or REJECTED(4)");
        }
        Integer currentStatus = ideaRepository.getStatus(id).orElse(null);
        // отклонять и одобрять можно только идеи на рассмотрении
        if (currentStatus == null || currentStatus != Status.UNDER_CONSIDERATION.getValue()) {
            log.info("Change status for idea id={} is not allowed. Current status is: {}", id, currentStatus);
            throw new StatusNotChangedException("Change status for idea id=" + id + " is not allowed");
        }
        ideaRepository.changeStatus(id, status);
        log.info("Status of idea with id={} has been changed to {}", id, status);
        return HttpStatus.OK;
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
