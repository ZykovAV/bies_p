package ylab.bies.fileStorageService.config;

import io.minio.MinioClient;
import lombok.Getter;
import lombok.Setter;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
@ConfigurationProperties(prefix = "s3.minio")
@Getter
@Setter
public class MinioConfig {

  private String endpoint;
  private String accessKey;
  private String secretKey;
  private String bucket;

  @Bean
  public MinioClient minioClient() {
    return MinioClient.builder()
            .endpoint(endpoint)
            .credentials(accessKey, secretKey)
            .build();
  }
}
