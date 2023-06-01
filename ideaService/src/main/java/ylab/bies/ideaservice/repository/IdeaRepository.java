package ylab.bies.ideaservice.repository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import ylab.bies.ideaservice.entity.Idea;


import java.util.Optional;

public interface IdeaRepository extends JpaRepository<Idea, Long> {
    Optional<Idea> findById(Long id);

    @Query("UPDATE Idea i SET i.status=:status WHERE i.id=:id AND i.status=2")
    void changeStatus(Long id, Integer status);

    @Query("SELECT i.status FROM Idea i WHERE i.id=:id")
    Optional<Integer> getStatus(Long id);

    Page<Idea> findAllByStatusNotOrderByRatingDesc(int i, Pageable pageable);

    @Modifying(clearAutomatically = true)
    @Query("update Idea  i set i.name = :newName, i.text = :newText, i.status = :newStatus where i.id = :id")
    void updateIdeaById(@Param("id") Long id, @Param("newName") String name, @Param("newText") String text,
                        @Param("newStatus") int status);
}
