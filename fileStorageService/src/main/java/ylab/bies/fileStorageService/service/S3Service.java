package ylab.bies.fileStorageService.service;

import org.springframework.web.multipart.MultipartFile;

public interface S3Service {
  void putObject(String bucketName, String key, MultipartFile file) throws Exception;

  void removeObject(String bucketName, String key) throws Exception;

  byte[] getObject(String bucketName, String key, long size) throws Exception;
}
