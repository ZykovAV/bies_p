package ylab.bies.userservice.entity;

import lombok.Getter;
import lombok.Setter;

import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;
import javax.validation.constraints.NotBlank;

@Entity
@Getter
@Setter
@Table(name = "roles")
public class Role {
    @Id
    private Long id;
    @NotBlank
    private String name;
}
