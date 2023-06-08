package ylab.bies.userservice.repository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.transaction.annotation.Transactional;
import ylab.bies.userservice.entity.User;
import ylab.bies.userservice.projection.UserProjection;

import java.util.Optional;
import java.util.UUID;

@Transactional(readOnly = true)
public interface UserRepository extends JpaRepository<User, UUID> {
    Optional<UserProjection> findProjectedById(UUID uuid);

    Page<UserProjection> findAllProjectedBy(Pageable pageable);
}
