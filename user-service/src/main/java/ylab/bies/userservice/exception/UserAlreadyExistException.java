package ylab.bies.userservice.exception;

import org.springframework.http.HttpStatus;

public class UserAlreadyExistException extends ApplicationException {
    public UserAlreadyExistException(String message) {
        super(HttpStatus.CONFLICT, message);
    }
}
