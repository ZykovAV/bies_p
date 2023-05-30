package ylab.bies.fileStorageService.service;

import org.springframework.web.multipart.MultipartFile;
import ylab.bies.fileStorageService.dto.FileListByIdeaDto;
import ylab.bies.fileStorageService.entity.FileModel;

import java.util.Optional;
import java.util.UUID;

public interface FileService {

  void addFile(Long ideaId, MultipartFile file, String bearerToken);

  FileListByIdeaDto getFileListByIdeaId(Long ideaId);

  Optional<FileModel> getByFileId(UUID fileId);

  void removeFile(UUID fileId);

  FileModel getFileWithBodyById(UUID fileId);
}
