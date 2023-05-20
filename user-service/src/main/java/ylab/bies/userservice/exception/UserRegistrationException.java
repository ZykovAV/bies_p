package ylab.bies.userservice.exception;

import org.springframework.http.HttpStatus;

public class UserRegistrationException extends ApplicationException {
    public UserRegistrationException(String message) {
        super(HttpStatus.INTERNAL_SERVER_ERROR, message);
    }
}
