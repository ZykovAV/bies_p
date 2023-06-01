package ylab.bies.ideaservice.entity;

import javax.persistence.EmbeddedId;
import javax.persistence.Entity;
import javax.persistence.Table;
import lombok.Getter;
import lombok.Setter;

import java.util.UUID;

@Getter
@Setter
@Entity
@Table(name = "votes")
public class Vote {

    @EmbeddedId
    private VotePK pk;
    private Boolean isLike;

    public Vote(UUID userId, Long ideaId, Boolean isLike) {
        pk = new VotePK();
        pk.setUserId(userId);
        pk.setIdeaId(ideaId);
        this.isLike = isLike;
    }

    public Vote() {
        pk = new VotePK();
    }
}
