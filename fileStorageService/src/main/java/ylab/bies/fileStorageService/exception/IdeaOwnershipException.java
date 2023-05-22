package ylab.bies.fileStorageService.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(code = HttpStatus.FORBIDDEN)
public class IdeaOwnershipException extends RuntimeException {

  public IdeaOwnershipException(Long ideaId) {
    super("User with credentials provided is not allowed to edit idea " + ideaId);
  }

}
