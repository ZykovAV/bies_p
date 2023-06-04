package ylab.bies.fileStorageService.config;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@Configuration
@ConfigurationProperties(prefix = "idea-service")
@Getter
@Setter
public class IdeaServiceConfig {

  private String baseUrl;
  private String validateIdeaOwnerEndpoint;

}
