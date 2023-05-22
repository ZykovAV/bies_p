package ylab.bies.ideaservice.service.impl;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import ylab.bies.ideaservice.dto.request.IdeaDraftRequestDto;
import ylab.bies.ideaservice.dto.response.IdeaDraftResponseDto;
import ylab.bies.ideaservice.entity.Idea;
import ylab.bies.ideaservice.exception.UnauthorizedException;
import ylab.bies.ideaservice.mapper.IdeaMapper;
import ylab.bies.ideaservice.repository.IdeaRepository;
import ylab.bies.ideaservice.service.IdeaService;
import ylab.bies.ideaservice.service.VoteService;
import ylab.bies.ideaservice.util.Decoder;

import java.util.UUID;

import static ylab.bies.ideaservice.util.constants.StatusConstants.DRAFT;

@Slf4j
@Service
public class IdeaServiceImpl implements IdeaService {
    private final VoteService voteService;
    private final IdeaRepository ideaRepository;
    private final Decoder decoder;
    private final IdeaMapper ideaMapper;

    @Autowired
    public IdeaServiceImpl(VoteService voteService, IdeaRepository ideaRepository, Decoder decoder, IdeaMapper ideaMapper) {
        this.voteService = voteService;
        this.ideaRepository = ideaRepository;
        this.decoder = decoder;
        this.ideaMapper = ideaMapper;
    }

    public IdeaDraftResponseDto createDraftIdea(String token, IdeaDraftRequestDto draftRequestDto) {
        UUID userId = decoder.getUuidFromToken(token);
        if (userId == null) {
            log.error("Invalid auth-token: Unauthorized");
            throw new UnauthorizedException("Invalid auth-token: Unauthorized");
        }
        Idea draft = ideaMapper.ideaDraftRequestDtoToIdeaEntity(draftRequestDto);
        draft.setUserId(userId);
        draft.setStatusId(DRAFT);
        Idea savedDraft = ideaRepository.save(draft);
        log.info("Draft saved: {}", draft);
        return ideaMapper.ideaEntityToIdeaDraftResponseDto(savedDraft);
    }
}
