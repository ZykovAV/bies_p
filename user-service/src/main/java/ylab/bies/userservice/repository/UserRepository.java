package ylab.bies.userservice.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.transaction.annotation.Transactional;
import ylab.bies.userservice.entity.User;

import java.util.UUID;

@Transactional(readOnly = true)
public interface UserRepository extends JpaRepository<User, UUID> {
}
