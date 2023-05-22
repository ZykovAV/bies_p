package ylab.bies.ideaservice.entity;

import javax.persistence.Embeddable;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;

import java.io.Serializable;
import java.util.UUID;

@Getter
@Setter
@EqualsAndHashCode
@Embeddable
public class VotePK implements Serializable {
    private Long ideaId;
    private UUID userId;
}
