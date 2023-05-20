package ylab.bies.userservice.dto;

import lombok.Getter;
import lombok.Setter;
import org.springframework.http.HttpStatus;

import java.time.LocalDateTime;

@Getter
@Setter
public class ExceptionResponse {
    private LocalDateTime timeStamp;
    private HttpStatus status;
    private int statusCode;
    private String message;
    private String path;
}
