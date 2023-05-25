package ylab.bies.fileStorageService.service.impl;

import io.minio.MinioClient;
import io.minio.PutObjectArgs;
import io.minio.RemoveObjectArgs;
import io.minio.errors.*;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import ylab.bies.fileStorageService.config.S3Config;
import ylab.bies.fileStorageService.service.S3Service;

import java.io.IOException;
import java.io.InputStream;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;

@Service
@Slf4j
public class MinioServiceImpl implements S3Service {

  private final MinioClient minioClient;
  private final S3Config s3Config;

  public MinioServiceImpl(S3Config s3Config) {
    this.s3Config = s3Config;
    minioClient = MinioClient.builder()
            .endpoint(s3Config.getUrl())
            .credentials(s3Config.getLogin(), s3Config.getPassword())
            .build();
  }

  @Override
  public void putObject(String bucketName, String key, MultipartFile file) throws IOException, ServerException,
          InsufficientDataException, ErrorResponseException, NoSuchAlgorithmException, InvalidKeyException,
          InvalidResponseException, XmlParserException, InternalException {
    try (InputStream inputStream = file.getInputStream()) {
      PutObjectArgs putObjectArgs = PutObjectArgs.builder()
              .bucket(bucketName)
              .object(key)
              .stream(inputStream, inputStream.available(), -1)
              .contentType(file.getContentType())
              .build();
      minioClient.putObject(putObjectArgs);
      log.info("File {} sent to S3 bucket {}", key, bucketName);
    }
  }

  @Override
  public void removeObject(String bucketName, String key) throws ServerException, InsufficientDataException,
          ErrorResponseException, IOException, NoSuchAlgorithmException, InvalidKeyException, InvalidResponseException,
          XmlParserException, InternalException {
    RemoveObjectArgs removeObjectArgs = RemoveObjectArgs.builder()
            .bucket(bucketName)
            .object(key)
            .build();
    minioClient.removeObject(removeObjectArgs);
  }

}
