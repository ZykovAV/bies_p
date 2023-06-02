package ylab.bies.ideaservice.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import ylab.bies.ideaservice.entity.Vote;
import ylab.bies.ideaservice.entity.VotePK;

import java.util.UUID;

public interface VoteRepository extends JpaRepository<Vote, VotePK> {

    @Query("SELECT v.isLike FROM Vote v WHERE v.pk.ideaId=:ideaId AND v.pk.userId=:userId")
    Boolean getVoteOfUser(UUID userId, Long ideaId);

    @Query("SELECT COUNT(v) FROM Vote v WHERE v.pk.ideaId=:ideaId AND v.isLike=true")
    int getLikesCount(Long ideaId);

    @Query("SELECT COUNT(v) FROM Vote v WHERE v.pk.ideaId=:ideaId AND v.isLike=false")
    int getDislikesCount(Long ideaId);

    Vote save(Vote vote);

    @Query("UPDATE Vote v SET v.isLike=:isLike WHERE v.pk.ideaId=:ideaId AND v.pk.userId=:userId")
    void changeVote(UUID userId, Long id, boolean isLike);
}
