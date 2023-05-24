package ylab.bies.ideaservice.entity;

import lombok.*;

import javax.persistence.*;
import java.util.UUID;


@Entity
@Table(name = "ideas")
@Getter
@Setter
public class Idea {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private Long id;

    @Column(name = "name", nullable = false)
    private String name;

    @Column(name = "text", nullable = false)
    private String text;

    @Column(name = "rating")
    private Integer rating;

    @Column(name = "user_id", nullable = false)
    private UUID userId;

    @Column(name = "status")
    private Integer status;

}
