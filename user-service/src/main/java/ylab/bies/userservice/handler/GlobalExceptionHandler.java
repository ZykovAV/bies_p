package ylab.bies.userservice.handler;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import ylab.bies.userservice.dto.ExceptionResponse;
import ylab.bies.userservice.exception.ApplicationException;

import javax.servlet.http.HttpServletRequest;
import java.time.LocalDateTime;

@RestControllerAdvice
public class GlobalExceptionHandler {
    @ExceptionHandler(ApplicationException.class)
    public ResponseEntity<ExceptionResponse> handleApplicationException(ApplicationException exception,
                                                                        HttpServletRequest request) {
        ExceptionResponse exceptionResponse = getExceptionResponse(exception, request);
        return ResponseEntity.status(exceptionResponse.getStatus()).body(exceptionResponse);
    }

    private ExceptionResponse getExceptionResponse(ApplicationException exception, HttpServletRequest request) {
        ExceptionResponse exceptionResponse = new ExceptionResponse();
        exceptionResponse.setTimeStamp(LocalDateTime.now());
        exceptionResponse.setStatus(exception.getStatus());
        exceptionResponse.setStatusCode(exception.getRawStatusCode());
        exceptionResponse.setMessage(exception.getMessage());
        exceptionResponse.setPath(request.getRequestURI());
        return exceptionResponse;
    }
}
