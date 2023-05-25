package ylab.bies.fileStorageService.config;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@Configuration
@ConfigurationProperties(prefix = "s3")
@Getter
@Setter
public class S3Config {

  private String url;
  private String login;
  private String password;
  private String ideaFilesBucket;

}
