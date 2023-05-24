package ylab.bies.ideaservice.service.impl;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import ylab.bies.ideaservice.dto.request.IdeaDraftRequestDto;
import ylab.bies.ideaservice.dto.response.IdeaDraftResponseDto;
import ylab.bies.ideaservice.dto.response.IdeaResponseDto;
import ylab.bies.ideaservice.entity.Idea;
import ylab.bies.ideaservice.mapper.IdeaMapper;
import ylab.bies.ideaservice.repository.IdeaRepository;
import ylab.bies.ideaservice.service.IdeaService;
import ylab.bies.ideaservice.service.VoteService;
import ylab.bies.ideaservice.util.AccessTokenDecoder;
import ylab.bies.ideaservice.util.enums.Status;

import javax.transaction.Transactional;
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
