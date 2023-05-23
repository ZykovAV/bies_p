package ylab.bies.userservice.config;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

import java.util.Set;

@Configuration
@ConfigurationProperties("application")
@Getter
@Setter
public class ApplicationConfiguration {
    private Set<String> userDefaultRoles;
}
