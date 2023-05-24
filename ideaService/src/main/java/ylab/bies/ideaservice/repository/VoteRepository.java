package ylab.bies.ideaservice.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import ylab.bies.ideaservice.entity.Vote;
import ylab.bies.ideaservice.entity.VotePK;

import java.util.UUID;

public interface VoteRepository extends JpaRepository<Vote, VotePK> {

    @Query("SELECT v.isLike FROM Vote v WHERE v.pk.ideaId=:ideaId AND v.pk.userId=:userId")
    Boolean getVoteOfUser(Long ideaId, UUID userId);

}
