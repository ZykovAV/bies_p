package ylab.bies.ideaservice.repository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import ylab.bies.ideaservice.entity.Idea;


import java.util.Optional;

public interface IdeaRepository extends JpaRepository<Idea, Long> {
    Optional<Idea> findById(Long id);

    @Query("UPDATE Idea i SET i.status=:status WHERE i.id=:id AND i.status=2")
    void changeStatus(Long id, Integer status);

    @Query("SELECT i.status FROM Idea i WHERE i.id=:id")
    Optional<Integer> getStatus(Long id);

    Page<Idea> findAllByStatusNotOrderByRatingDesc(int i, Pageable pageable);
}
