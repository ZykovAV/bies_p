package ylab.bies.fileStorageService.service.impl;

import org.springframework.stereotype.Service;
import ylab.bies.fileStorageService.service.IdeaServiceClient;

@Service
public class IdeaServiceClientImpl implements IdeaServiceClient {
  @Override
  public boolean validateIdeaOwner(Long ideaId, String token) {
    //TODO: to be implemented
    return true;
  }
}
