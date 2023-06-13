package ylab.bies.ideaservice.service.impl;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import ylab.bies.ideaservice.exception.VoteRejectedException;
import ylab.bies.ideaservice.repository.VoteRepository;
import ylab.bies.ideaservice.service.VoteService;
import ylab.bies.ideaservice.entity.Vote;

import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class VoteServiceImpl implements VoteService {

    private final VoteRepository voteRepository;

    @Override
    public int getRating(Long ideaId) {
        return voteRepository.getLikesCount(ideaId) - voteRepository.getDislikesCount(ideaId);
    }

    @Override
    public Boolean getVoteOfUser(UUID userId, Long ideaId) {
        return voteRepository.getVoteOfUser(userId, ideaId);
    }

    @Override
    public void rate(UUID userId, Long ideaId, boolean isLike) {
        Boolean vote = voteRepository.getVoteOfUser(userId, ideaId);
        if (vote == null) {
            voteRepository.save(new Vote(userId, ideaId, isLike));
        } else if (vote != isLike) {
            voteRepository.changeVote(userId, ideaId, isLike);
        } else {
            log.info("Second rate an idea with id={} is not allowed.", ideaId);
            throw new VoteRejectedException("Cannot rate for the second time");
        }
    }
}
