package ylab.bies.fileStorageService.handler;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import ylab.bies.fileStorageService.exception.ErrorResponse;
import ylab.bies.fileStorageService.exception.IdeaOwnershipException;
import ylab.bies.fileStorageService.exception.InvalidSaveRequestException;
import ylab.bies.fileStorageService.exception.RequestedFileNotFoundException;

import javax.servlet.http.HttpServletRequest;
import java.time.LocalDateTime;

@RestControllerAdvice
public class DefaultExceptionHandler {

  @ExceptionHandler(RequestedFileNotFoundException.class)
  public ResponseEntity<ErrorResponse> handleRequestedFileNotFoundException(RequestedFileNotFoundException e,
                                                                            HttpServletRequest request) {
    ErrorResponse errorResponse = new ErrorResponse(
            request.getRequestURI(),
            e.getMessage(),
            HttpStatus.NOT_FOUND.value(),
            LocalDateTime.now()
    );

    return new ResponseEntity<>(errorResponse, HttpStatus.NOT_FOUND);
  }

  @ExceptionHandler(IdeaOwnershipException.class)
  public ResponseEntity<ErrorResponse> handleIdeaOwnershipException(IdeaOwnershipException e,
                                                                    HttpServletRequest request) {
    ErrorResponse errorResponse = new ErrorResponse(
            request.getRequestURI(),
            e.getMessage(),
            HttpStatus.FORBIDDEN.value(),
            LocalDateTime.now()
    );

    return new ResponseEntity<>(errorResponse, HttpStatus.FORBIDDEN);
  }

  @ExceptionHandler(InvalidSaveRequestException.class)
  public ResponseEntity<ErrorResponse> handleIllegalSaveRequestException(InvalidSaveRequestException e,
                                                                         HttpServletRequest request) {
    ErrorResponse errorResponse = new ErrorResponse(
            request.getRequestURI(),
            e.getMessage(),
            HttpStatus.BAD_REQUEST.value(),
            LocalDateTime.now()
    );

    return new ResponseEntity<>(errorResponse, HttpStatus.BAD_REQUEST);
  }

  @ExceptionHandler(Exception.class)
  public ResponseEntity<ErrorResponse> handleException(Exception e,
                                                       HttpServletRequest request) {
    ErrorResponse errorResponse = new ErrorResponse(
            request.getRequestURI(),
            e.getMessage(),
            HttpStatus.INTERNAL_SERVER_ERROR.value(),
            LocalDateTime.now()
    );

    return new ResponseEntity<>(errorResponse, HttpStatus.INTERNAL_SERVER_ERROR);
  }

}
