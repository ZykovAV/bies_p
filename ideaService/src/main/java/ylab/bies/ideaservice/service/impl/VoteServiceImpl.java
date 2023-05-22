package ylab.bies.ideaservice.service.impl;

import org.springframework.stereotype.Service;
import ylab.bies.ideaservice.repository.VoteRepository;
import ylab.bies.ideaservice.service.VoteService;

import java.util.UUID;

@Service
public class VoteServiceImpl implements VoteService {
    private final VoteRepository voteRepository;

    public VoteServiceImpl(VoteRepository voteRepository) {
        this.voteRepository = voteRepository;
    }

    public Boolean getVoteOfUser(Long ideaId, UUID userId) {
        return voteRepository.getVoteOfUser(ideaId, userId);
    }
}
