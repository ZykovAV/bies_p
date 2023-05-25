package ylab.bies.ideaservice.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import ylab.bies.ideaservice.entity.Idea;

import java.util.List;
import java.util.Optional;

public interface IdeaRepository extends JpaRepository<Idea, Long> {
    Optional<Idea> findById(Long id);

    @Query(value = "select * from ideas i where i.status != 1 order by i.rating desc", nativeQuery = true)
    List<Idea> findAllIdeas();


}
