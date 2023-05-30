package ylab.bies.ideaservice.repository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import ylab.bies.ideaservice.entity.Idea;


import java.util.Optional;

public interface IdeaRepository extends JpaRepository<Idea, Long> {
    Optional<Idea> findById(Long id);


    Page<Idea> findAllByStatusNotOrderByRatingDesc(int i, Pageable pageable);
}
