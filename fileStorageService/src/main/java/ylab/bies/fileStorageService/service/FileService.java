package ylab.bies.fileStorageService.service;

import io.minio.errors.*;
import org.springframework.web.multipart.MultipartFile;
import ylab.bies.fileStorageService.dto.FileListByIdeaDto;
import ylab.bies.fileStorageService.entity.FileModel;

import java.io.IOException;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.util.Optional;
import java.util.UUID;

public interface FileService {

  void addFile(Long ideaId, MultipartFile file, String bearerToken)
          throws ServerException, InsufficientDataException, ErrorResponseException, IOException, NoSuchAlgorithmException, InvalidKeyException, InvalidResponseException, XmlParserException, InternalException;

  FileListByIdeaDto getFileListByIdeaId(Long ideaId);

  Optional<FileModel> getByFileId(UUID fileId);
}
