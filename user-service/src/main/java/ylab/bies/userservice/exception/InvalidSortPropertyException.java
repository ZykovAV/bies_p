package ylab.bies.userservice.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(HttpStatus.BAD_REQUEST)
public class InvalidSortPropertyException extends RuntimeException {
    public InvalidSortPropertyException(String message) {
        super(message);
    }
}

