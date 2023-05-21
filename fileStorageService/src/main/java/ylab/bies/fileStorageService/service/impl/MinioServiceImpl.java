package ylab.bies.fileStorageService.service.impl;

import io.minio.MinioClient;
import io.minio.PutObjectArgs;
import io.minio.errors.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import ylab.bies.fileStorageService.service.S3Service;

import java.io.IOException;
import java.io.InputStream;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;

@Service
public class MinioServiceImpl implements S3Service {

  private final Logger logger = LoggerFactory.getLogger(MinioServiceImpl.class);
  private final MinioClient minioClient;

  public MinioServiceImpl(MinioClient minioClient) {
    this.minioClient = minioClient;
  }

  @Override
  public void putObject(String bucketName, String key, MultipartFile file) throws IOException, ServerException, InsufficientDataException, ErrorResponseException, NoSuchAlgorithmException, InvalidKeyException, InvalidResponseException, XmlParserException, InternalException {
    try (InputStream inputStream = file.getInputStream()) {
      PutObjectArgs putObjectArgs = PutObjectArgs.builder()
              .bucket(bucketName)
              .object(key)
              .stream(inputStream, inputStream.available(), -1)
              .contentType(file.getContentType())
              .build();
      minioClient.putObject(putObjectArgs);
      logger.info("File {} sent to S3 bucket {}", key, bucketName);
    }
  }

}
