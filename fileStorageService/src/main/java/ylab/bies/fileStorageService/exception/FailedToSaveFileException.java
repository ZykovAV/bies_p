package ylab.bies.fileStorageService.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(code = HttpStatus.INTERNAL_SERVER_ERROR)
public class FailedToSaveFileException extends RuntimeException{

  public FailedToSaveFileException() {
    super("Failed to save file.");
  }
}
