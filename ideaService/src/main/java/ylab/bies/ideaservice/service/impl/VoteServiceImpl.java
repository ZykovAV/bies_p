package ylab.bies.ideaservice.service.impl;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import ylab.bies.ideaservice.repository.VoteRepository;
import ylab.bies.ideaservice.service.VoteService;

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
}
