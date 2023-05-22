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
@Table(name = "votes", schema = "idea_db")
public class Vote {

    @EmbeddedId
    private VotePK pk;
    private Boolean isLike;

    public Vote(Long ideaId, UUID userId) {
        pk = new VotePK();
        pk.setIdeaId(ideaId);
        pk.setUserId(userId);
    }

    public Vote() {
        pk = new VotePK();
    }
}
