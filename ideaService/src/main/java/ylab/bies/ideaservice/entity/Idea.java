package ylab.bies.ideaservice.entity;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

import javax.persistence.*;
import javax.validation.constraints.NotNull;
import java.util.Objects;
import java.util.UUID;

@Getter
@Setter
@NoArgsConstructor
@ToString
@Entity
@Table(name = "ideas", schema = "idea_db")
public class Idea {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String name;

    @Column(nullable = false)
    private String text;

    private Integer rating;

    @Column(nullable = false)
    private UUID userId;

    private Long statusId;

    public Idea(UUID userId) {
        this.userId = userId;
        this.rating = 0;
        this.statusId = 1L;
    }

    public Idea(UUID userId, String name, String text) {
        this.userId = userId;
        this.name = name;
        this.text = text;
        this.rating = 0;
        this.statusId = 1L;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        Idea idea = (Idea) o;

        if (!id.equals(idea.id)) return false;
        if (!Objects.equals(name, idea.name)) return false;
        if (!Objects.equals(text, idea.text)) return false;
        return userId.equals(idea.userId);
    }

    @Override
    public int hashCode() {
        int result = id.hashCode();
        result = 31 * result + (name != null ? name.hashCode() : 0);
        result = 31 * result + (text != null ? text.hashCode() : 0);
        result = 31 * result + userId.hashCode();
        return result;
    }
}
