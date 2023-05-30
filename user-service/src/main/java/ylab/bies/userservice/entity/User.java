package ylab.bies.userservice.entity;

import io.smallrye.common.constraint.Nullable;
import lombok.Getter;
import lombok.Setter;

import javax.persistence.*;
import javax.validation.constraints.Email;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.Pattern;
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
    @NotBlank(message = "Email can't be null or empty")
    @Size(max = 32, message = "Email can't be more than {max} characters")
    @Email(message = "Email must have valid format")
    private String email;

    @Column(name = "first_name", nullable = false)
    @NotBlank(message = "First name can't be null or empty")
    @Size(max = 32, message = "First name can't be more than {max} characters")
    @Pattern(regexp = "\\p{L}+", message = "First name can't contains any non-alphabetic characters")
    private String firstName;

    @Column(name = "last_name", nullable = false)
    @NotBlank(message = "Last name can't be null or empty")
    @Size(max = 32, message = "Last name can't be more than {max} characters")
    @Pattern(regexp = "\\p{L}+", message = "Last name can't contains any non-alphabetic characters")
    private String lastName;

    @Column(name = "middle_name")
    @Nullable
    @Size(max = 32, message = "Middle name can't be more than {max} characters")
    @Pattern(regexp = "\\p{L}+", message = "Middle name can't contains any non-alphabetic characters")
    private String middleName;

    @ManyToMany(fetch = FetchType.EAGER)
    @JoinTable(
            name = "user_roles",
            joinColumns = @JoinColumn(name = "user_id"),
            inverseJoinColumns = @JoinColumn(name = "role_id")
    )
    private Set<Role> roles = new HashSet<>();
}
