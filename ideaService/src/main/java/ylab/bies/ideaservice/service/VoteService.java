package ylab.bies.ideaservice.service;

import java.util.UUID;

public interface VoteService {
    Boolean getVoteOfUser(Long ideaId, UUID userId);
}
