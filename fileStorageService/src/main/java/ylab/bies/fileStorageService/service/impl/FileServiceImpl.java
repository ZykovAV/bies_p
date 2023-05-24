package ylab.bies.fileStorageService.service.impl;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;
import ylab.bies.fileStorageService.config.MinioConfig;
import ylab.bies.fileStorageService.dto.FileListByIdeaDto;
import ylab.bies.fileStorageService.entity.FileModel;
import ylab.bies.fileStorageService.exception.FailedToSaveFileException;
import ylab.bies.fileStorageService.exception.IdeaOwnershipException;
import ylab.bies.fileStorageService.exception.InvalidSaveRequestException;
import ylab.bies.fileStorageService.mapper.FileMapper;
import ylab.bies.fileStorageService.repository.FileRepository;
import ylab.bies.fileStorageService.service.FileService;
import ylab.bies.fileStorageService.service.IdeaServiceClient;
import ylab.bies.fileStorageService.service.S3Service;

import java.util.Optional;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
public class FileServiceImpl implements FileService {

  private final MinioConfig minioConfig;
  private final FileRepository fileRepository;
  private final S3Service s3Service;
  private final IdeaServiceClient ideaServiceClient;
  private final FileMapper mapper;

  @Transactional(rollbackFor = Exception.class)
  @Override
  public void addFile(Long ideaId, MultipartFile file, String bearerToken) {
    if (bearerToken == null || !ideaServiceClient.validateIdeaOwner(ideaId, bearerToken)) {
      throw new IdeaOwnershipException(ideaId);
    }

    FileModel fileModel = new FileModel(ideaId, file);
    if (fileModel.getFileName() == null || fileModel.getFileName().isEmpty() || fileModel.getIdeaId() == null) {
      throw new InvalidSaveRequestException();
    }

    fileRepository.save(fileModel);
    String objectKey = getObjectKey(fileModel.getIdeaId(), fileModel.getId());
    log.info("Id {} generated for file with name {} and idea id {}", fileModel.getId(), fileModel.getFileName(), ideaId);

    try {
      s3Service.putObject(minioConfig.getBucket(), objectKey, file);
    } catch (Exception e) {
      log.error("Failed to save file ", e);
      throw new FailedToSaveFileException();
    }
    log.info("Metadata for file with id {} saved to db", fileModel.getId());
  }

  private String getObjectKey(Long ideaId, UUID fileId) {
    return ideaId + "/" + fileId.toString();
  }

  @Transactional(readOnly = true)
  @Override
  public FileListByIdeaDto getFileListByIdeaId(Long ideaId) {
    FileListByIdeaDto result = new FileListByIdeaDto();
    result.setIdeaId(ideaId);
    result.setFiles(
            mapper.toFileDtoList(fileRepository.findAllByIdeaId(ideaId))
    );
    return result;
  }

  @Override
  public Optional<FileModel> getByFileId(UUID fileId) {
    return fileRepository.findById(fileId);
  }

}
