package ylab.bies.userservice.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.transaction.annotation.Transactional;
import ylab.bies.userservice.entity.Role;

import javax.validation.constraints.NotBlank;

@Transactional(readOnly = true)
public interface RoleRepository extends JpaRepository<Role, Long> {
    Role findByName(@NotBlank String name);
}
