package ylab.bies.fileStorageService.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(HttpStatus.NOT_FOUND)
public class RequestedFileNotFoundException extends RuntimeException {

  public RequestedFileNotFoundException() {
    super("Requested file not found");
  }
}
