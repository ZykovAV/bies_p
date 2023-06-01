package ylab.bies.ideaservice.service;

import java.util.UUID;

public interface VoteService {
    Boolean getVoteOfUser(UUID userId, Long ideaId);
    int getRating(Long ideaId);
    void rate(UUID userId, Long ideaId, boolean isLike);
}
