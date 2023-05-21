package ylab.bies.fileStorageService.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import ylab.bies.fileStorageService.entity.FileModel;

import java.io.File;
import java.util.List;
import java.util.UUID;

public interface FileRepository extends JpaRepository<FileModel, UUID> {

  List<FileModel> findAllByIdeaId(Long ideaId);
}
