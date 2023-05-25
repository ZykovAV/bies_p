package ylab.bies.fileStorageService.service;

import io.minio.errors.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;

public interface S3Service {
  void putObject(String bucketName, String key, MultipartFile file) throws IOException, ServerException,
          InsufficientDataException, ErrorResponseException, NoSuchAlgorithmException, InvalidKeyException,
          InvalidResponseException, XmlParserException, InternalException;

  void removeObject(String bucketName, String key) throws ServerException, InsufficientDataException,
          ErrorResponseException, IOException, NoSuchAlgorithmException, InvalidKeyException, InvalidResponseException,
          XmlParserException, InternalException;
}
