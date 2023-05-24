package ylab.bies.fileStorageService.service.impl;

import io.minio.errors.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;
import ylab.bies.fileStorageService.entity.FileModel;
import ylab.bies.fileStorageService.exception.IdeaOwnershipException;
import ylab.bies.fileStorageService.exception.InvalidSaveRequestException;
import ylab.bies.fileStorageService.repository.FileRepository;
import ylab.bies.fileStorageService.service.FileService;
import ylab.bies.fileStorageService.service.IdeaServiceClient;
import ylab.bies.fileStorageService.service.S3Service;

import java.io.IOException;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.util.UUID;

@Service
public class FileServiceImpl implements FileService {
  private final Logger logger = LoggerFactory.getLogger(FileServiceImpl.class);
  private final FileRepository fileRepository;
  private final S3Service s3Service;
  private final IdeaServiceClient ideaServiceClient;

  @Value("${s3.bucket}")
  private String bucket;

  public FileServiceImpl(FileRepository fileRepository, S3Service s3Service, IdeaServiceClient ideaServiceClient) {
    this.fileRepository = fileRepository;
    this.s3Service = s3Service;
    this.ideaServiceClient = ideaServiceClient;
  }

  @Transactional(rollbackFor = Exception.class)
  @Override
  public void addFile(Long ideaId, MultipartFile file, String bearerToken) throws ServerException, InsufficientDataException, ErrorResponseException, IOException, NoSuchAlgorithmException, InvalidKeyException, InvalidResponseException, XmlParserException, InternalException {
    if (bearerToken == null || !ideaServiceClient.validateIdeaOwner(ideaId, bearerToken)) {
      throw new IdeaOwnershipException(ideaId);
    }

    FileModel fileModel = new FileModel(ideaId, file);
    if (fileModel.getFileName() == null || fileModel.getFileName().isEmpty() || fileModel.getIdeaId() == null) {
      throw new InvalidSaveRequestException();
    }

    fileRepository.save(fileModel);
    String objectKey = getObjectKey(fileModel.getIdeaId(), fileModel.getId());
    logger.info("Id {} generated for file with name {} and idea id {}", fileModel.getId(), fileModel.getFileName(), ideaId);

    s3Service.putObject(bucket, objectKey, file);
    logger.info("Metadata for file with id {} saved to db", fileModel.getId());
  }

  private String getObjectKey(Long ideaId, UUID fileId) {
    return ideaId + "/" + fileId.toString();
  }


}