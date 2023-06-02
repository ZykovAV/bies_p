package ylab.bies.fileStorageService.service.impl;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;
import ylab.bies.fileStorageService.config.S3Config;
import ylab.bies.fileStorageService.dto.FileListByIdeaDto;
import ylab.bies.fileStorageService.entity.FileModel;
import ylab.bies.fileStorageService.exception.IdeaOwnershipException;
import ylab.bies.fileStorageService.exception.InvalidSaveRequestException;
import ylab.bies.fileStorageService.exception.OperationFailedException;
import ylab.bies.fileStorageService.exception.RequestedFileNotFoundException;
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

  private final FileRepository fileRepository;
  private final S3Service s3Service;
  private final S3Config s3Config;
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
      s3Service.putObject(s3Config.getIdeaFilesBucket(), objectKey, file);
    } catch (Exception e) {
      log.error("Failed to save file ", e);
      throw new OperationFailedException("Failed to save file");
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

  @Transactional(readOnly = true)
  @Override
  public Optional<FileModel> getByFileId(UUID fileId) {
    return fileRepository.findById(fileId);
  }

  /**
   * Method for removing file by id both from database and from s3. </b>
   * If no files with this id were found in the database, then {@link RequestedFileNotFoundException} is thrown. </b>
   * If a file with this id is found in the db, but not found in s3 file storage - then no exceptions will
   * be thrown, and the item with this id will be removed from db quietly, so that db and s3 are consistent.
   * @param fileId - uuid of file to be removed
   */
  @Transactional(rollbackFor = Exception.class)
  @Override
  public void removeFile(UUID fileId) {
    FileModel fileModelToRemove = fileRepository.findById(fileId)
            .orElseThrow(RequestedFileNotFoundException::new);
    long ideaId = fileModelToRemove.getIdeaId();

    String dummyToken = "test";
    if (dummyToken == null || !ideaServiceClient.validateIdeaOwner(ideaId, dummyToken)) {
      throw new IdeaOwnershipException(ideaId);
    }

    fileRepository.delete(fileModelToRemove);

    String key = getObjectKey(fileModelToRemove.getIdeaId(), fileModelToRemove.getId());
    try {
      s3Service.removeObject(s3Config.getIdeaFilesBucket(), key);
    } catch (Exception e) {
      log.error("Failed to remove file {} from S3", key, e);
      throw new OperationFailedException("Failed to remove file");
    }
    log.info("File with {} was removed (or didn't exist) from S3", key);
  }

  @Transactional(readOnly = true)
  @Override
  public FileModel getFileWithBodyById(UUID fileId) {
    FileModel fileModel = fileRepository.findById(fileId)
            .orElseThrow(RequestedFileNotFoundException::new);
    String key = getObjectKey(fileModel.getIdeaId(), fileModel.getId());
    log.info("Download for {} ({} bytes) is starting", key, fileModel.getFileSize());

    try {
      fileModel.setBody(s3Service.getObject(s3Config.getIdeaFilesBucket(), key, fileModel.getFileSize()));
      log.info("Download for {} ({} bytes) has finished", key, fileModel.getBody().length);
      return fileModel;
    } catch (Exception e) {
      log.error("Failed to download file {} from S3", key, e);
      throw new OperationFailedException("Failed to download file");
    }
  }

}
