package ylab.bies.userservice.entity;

import lombok.Getter;
import lombok.Setter;
import org.springframework.lang.Nullable;

import javax.persistence.*;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.Size;
import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

@Entity
@Table(name = "users")
@Getter
@Setter
public class User {
    @Id
    @Column(name = "id")
    private UUID id;
    @Column(name = "email", nullable = false, unique = true)
    @NotBlank
    @Size(min = 3, max = 20)
    private String email;
    @Column(name = "first_name", nullable = false)
    @NotBlank
    @Size(min = 3, max = 20)
    private String firstName;
    @Column(name = "last_name", nullable = false)
    @NotBlank
    @Size(min = 3, max = 20)
    private String lastName;
    @Column(name = "middle_name")
    @Nullable
    private String middleName;
    @ManyToMany(fetch = FetchType.EAGER)
    @JoinTable(
            name = "user_roles",
            joinColumns = @JoinColumn(name = "user_id"),
            inverseJoinColumns = @JoinColumn(name = "role_id")
    )
    private Set<Role> roles = new HashSet<>();
}
