package ylab.bies.ideaservice.service.impl;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ylab.bies.ideaservice.dto.notification.NotificationDto;
import ylab.bies.ideaservice.dto.request.IdeaDraftRequestDto;
import ylab.bies.ideaservice.dto.request.IdeaRequestDto;
import ylab.bies.ideaservice.dto.response.IdeaDraftResponseDto;
import ylab.bies.ideaservice.dto.response.IdeaResponseDto;
import ylab.bies.ideaservice.entity.Idea;
import ylab.bies.ideaservice.exception.AccessDeniedException;
import ylab.bies.ideaservice.exception.IdeaNotFoundException;
import ylab.bies.ideaservice.exception.InvalidStatusIdeaException;
import ylab.bies.ideaservice.exception.StatusNotChangedException;
import ylab.bies.ideaservice.exception.VoteRejectedException;
import ylab.bies.ideaservice.mapper.IdeaMapper;
import ylab.bies.ideaservice.repository.IdeaRepository;
import ylab.bies.ideaservice.service.IdeaService;
import ylab.bies.ideaservice.service.KafkaProducerService;
import ylab.bies.ideaservice.service.VoteService;
import ylab.bies.ideaservice.util.AccessTokenDecoder;
import ylab.bies.ideaservice.util.enums.Status;

import java.time.LocalDateTime;
import java.util.Objects;
import java.util.UUID;

import static ylab.bies.ideaservice.util.enums.Status.*;


@Slf4j
@Service
@RequiredArgsConstructor
public class IdeaServiceImpl implements IdeaService {

    private final VoteService voteService;
    private final IdeaRepository ideaRepository;
    private final AccessTokenDecoder decoder;
    private final IdeaMapper ideaMapper;
    private final KafkaProducerService kafkaProducerService;
    public static final String PUBLISHED_IDEA = "Idea published";
    public static final String ACCEPTED_IDEA = "Idea accepted";
    public static final String REJECTED_IDEA = "Idea rejected";


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
    public void changeStatus(Long id, Integer status) {
        if (status == null ||
                (status != ACCEPTED.getValue() && status != Status.REJECTED.getValue())) {
            log.info("Status for idea id={} can be changed only to ACCEPTED(3) or REJECTED(4)", id);
            throw new StatusNotChangedException("Status can be changed only to ACCEPTED(3) or REJECTED(4)");
        }
        Integer currentStatus = ideaRepository.getStatus(id).orElse(null);
        // отклонять и одобрять можно только идеи на рассмотрении
        if (currentStatus == null || currentStatus != UNDER_CONSIDERATION.getValue()) {
            log.info("Change status for idea id={} is not allowed. Current status is: {}", id, currentStatus);
            throw new StatusNotChangedException("Change status for idea id=" + id + " is not allowed");
        }
        ideaRepository.changeStatus(id, status);
        String newStatus = status.equals(ACCEPTED.getValue()) ? ACCEPTED_IDEA : REJECTED_IDEA;
        NotificationDto notificationDto = createNotificationDto(newStatus, decoder.getUuidFromToken("test-token"), id);
        kafkaProducerService.sendNotification(notificationDto);
        log.info("Status of idea with id={} has been changed to '{}'", id, newStatus);
    }

    @Override
    @Transactional
    public void rate(String token, Long id, boolean isLike) {
        UUID userId = decoder.getUuidFromToken(token);
        UUID authorId = ideaRepository.getAuthorId(id).orElse(null);
        if (authorId == null) {  // проверка существования такой идеи (идей без автора не существует)
            log.info("There's no idea with id={}", id);
            throw new IdeaNotFoundException("There's no idea with id=" + id);
        } else if (authorId.equals(userId)) {  // если юзер оценивает свою идею
            log.info("Attempt to rate own idea with id={} was rejected", id);
            throw new VoteRejectedException("It is not allowed to rate your own idea");
        }
        voteService.rate(userId, id, isLike);
        updateRating(id);  // пересчёт рейтинга
    }


    private void updateRating(Long id) {
        ideaRepository.updateRating(id, voteService.getRating(id));
    }


    @Transactional(readOnly = true)
    public Page<IdeaResponseDto> getAllIdeas(Pageable pageable) {
        Page<Idea> ideas = ideaRepository.findAllByStatusNotOrderByRatingDesc(DRAFT.getValue(), pageable);
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
        draft.setRating(0);
        Idea savedDraft = ideaRepository.save(draft);
        log.info("Draft saved: {}", draft);
        return ideaMapper.ideaEntityToIdeaDraftResponseDto(savedDraft);
    }

    @Transactional
    public IdeaResponseDto updateIdea(String token, Long id, IdeaRequestDto ideaRequestDto) {
        UUID userId = decoder.getUuidFromToken(token);
        Idea ideaFromDB = ideaRepository.findById(id).orElseThrow(() ->
                new IdeaNotFoundException(String.format("Idea with id  %s not found in database", id)));
        log.info("Idea from DB {} ", ideaFromDB);

        ideaStatusAndUserIdVerification(userId, ideaFromDB);

        Idea ideaForUpdate = ideaMapper.ideaRequestDtoToIdeaEntity(ideaRequestDto);
        ideaRepository.updateIdeaById(ideaForUpdate.getId(), ideaForUpdate.getName(),
                ideaForUpdate.getText(), UNDER_CONSIDERATION.getValue());
        Idea updatedIdea = ideaRepository.findById(id).orElseThrow(() -> new IdeaNotFoundException(" Edited idea not found "));
        log.info(String.format("Idea %s edited successfully ", updatedIdea));

        NotificationDto notificationDto = createNotificationDto(PUBLISHED_IDEA, userId, updatedIdea.getId());
        kafkaProducerService.sendNotification(notificationDto);
        return ideaMapper.ideaEntityToIdeaResponseDto(updatedIdea);
    }


    private void ideaStatusAndUserIdVerification(UUID userId, Idea ideaFromDB) {
        if (!userId.equals(ideaFromDB.getUserId())) {
            throw new InvalidStatusIdeaException("You cannot change someone else's idea");
        }
        if (Objects.equals(ideaFromDB.getStatus(), ACCEPTED.getValue()) ||
                Objects.equals(ideaFromDB.getStatus(), REJECTED.getValue())) {
            throw new InvalidStatusIdeaException("Idea with the status 'Accepted' or 'Rejected' cannot be edited");
        }
    }

    private NotificationDto createNotificationDto(String action, UUID userId, Long ideaId) {
        LocalDateTime localDateTime = LocalDateTime.now();

        return new NotificationDto(localDateTime, action, ideaId, userId);
    }
}
