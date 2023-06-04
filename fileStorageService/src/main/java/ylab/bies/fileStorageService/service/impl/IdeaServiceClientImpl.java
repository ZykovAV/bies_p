package ylab.bies.fileStorageService.service.impl;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import ylab.bies.fileStorageService.config.IdeaServiceConfig;
import ylab.bies.fileStorageService.exception.OperationFailedException;
import ylab.bies.fileStorageService.service.IdeaServiceClient;
import ylab.bies.fileStorageService.util.AccessTokenManager;

@Service
@Slf4j
public class IdeaServiceClientImpl implements IdeaServiceClient {
  private final WebClient.Builder webClientBuilder;
  private final IdeaServiceConfig ideaServiceConfig;
  public final AccessTokenManager accessTokenManager;

  public IdeaServiceClientImpl(IdeaServiceConfig ideaServiceConfig, AccessTokenManager accessTokenManager) {
    this.ideaServiceConfig = ideaServiceConfig;
    this.accessTokenManager = accessTokenManager;
    this.webClientBuilder = WebClient.builder()
            .baseUrl(ideaServiceConfig.getBaseUrl());
  }

  @Override
  public boolean validateIdeaOwner(Long ideaId) {
    try {
      return webClientBuilder.build()
              .get()
              .uri(String.format(ideaServiceConfig.getValidateIdeaOwnerEndpoint(), ideaId))
              .headers(h -> h.setBearerAuth(accessTokenManager.getJwtFromContext().getTokenValue()))
              .retrieve()
              .bodyToMono(Boolean.class)
              .block();
    } catch (Exception e) {
      log.error("Failed to validated idea owner", e);
      throw new OperationFailedException("Failed to validate idea owner.");
    }
  }
}
