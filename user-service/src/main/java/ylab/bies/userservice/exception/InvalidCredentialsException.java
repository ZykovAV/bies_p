package ylab.bies.userservice.exception;

import org.springframework.http.HttpStatus;

public class InvalidCredentialsException extends ApplicationException {
    public InvalidCredentialsException(String message) {
        super(HttpStatus.UNPROCESSABLE_ENTITY, message);
    }
}
