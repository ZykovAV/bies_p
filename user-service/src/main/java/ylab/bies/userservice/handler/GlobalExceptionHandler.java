package ylab.bies.userservice.handler;

import org.springframework.context.support.DefaultMessageSourceResolvable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import ylab.bies.userservice.dto.ExceptionResponse;
import ylab.bies.userservice.exception.ApplicationException;

import javax.servlet.http.HttpServletRequest;
import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

@RestControllerAdvice
public class GlobalExceptionHandler {
    @ExceptionHandler(ApplicationException.class)
    public ResponseEntity<ExceptionResponse> handleApplicationException(ApplicationException exception,
                                                                        HttpServletRequest request) {
        ExceptionResponse exceptionResponse = getExceptionResponse(
                request,
                exception.getStatus(),
                Collections.singletonList(exception.getMessage())
        );
        return ResponseEntity.status(exceptionResponse.getStatus()).body(exceptionResponse);
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ExceptionResponse> handleMethodArgumentNotValid(MethodArgumentNotValidException exception,
                                                                          HttpServletRequest request) {
        ExceptionResponse exceptionResponse = getExceptionResponse(
                request,
                HttpStatus.BAD_REQUEST,
                getFieldValidationMessage(exception.getFieldErrors())
        );
        return ResponseEntity.status(exceptionResponse.getStatus()).body(exceptionResponse);
    }

    private ExceptionResponse getExceptionResponse(HttpServletRequest request,
                                                   HttpStatus status,
                                                   List<String> messages) {
        ExceptionResponse exceptionResponse = new ExceptionResponse();
        exceptionResponse.setTimeStamp(LocalDateTime.now());
        exceptionResponse.setStatus(status);
        exceptionResponse.setStatusCode(status.value());
        exceptionResponse.setMessages(messages);
        exceptionResponse.setPath(request.getRequestURI());
        return exceptionResponse;
    }

    private List<String> getFieldValidationMessage(List<FieldError> fieldErrors) {
        return fieldErrors.stream()
                .map(DefaultMessageSourceResolvable::getDefaultMessage)
                .collect(Collectors.toList());
    }
}
