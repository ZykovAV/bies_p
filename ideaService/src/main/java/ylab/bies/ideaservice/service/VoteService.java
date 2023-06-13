package ylab.bies.ideaservice.service;

import java.util.UUID;

/**
 * Service for like/dislike ideas, counting rating
 */
public interface VoteService {
    /**
     * Return vote of user
     * @param userId - id of user
     * @param ideaId - id of idea
     * @return 'true' if user liked, 'false' - if disliked, 'null' - if there's no rate of this user
     */
    Boolean getVoteOfUser(UUID userId, Long ideaId);

    /**
     * Return rating (count of likes minus count of dislikes) of idea
     * @param ideaId - id of idea
     * @return rating of idea
     */
    int getRating(Long ideaId);

    /**
     * Save user's rate
     * @param userId - id of user
     * @param ideaId - id of idea
     * @param isLike - 'true' if user like, 'false' - if dislike
     */
    void rate(UUID userId, Long ideaId, boolean isLike);
}
