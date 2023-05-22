package ylab.bies.fileStorageService.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(code = HttpStatus.BAD_REQUEST)
public class InvalidSaveRequestException extends RuntimeException {

  public InvalidSaveRequestException() {
    super("Idea id and file name must not be empty.");
  }

}
