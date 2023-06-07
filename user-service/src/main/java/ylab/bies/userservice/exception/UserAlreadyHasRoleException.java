package ylab.bies.userservice.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(HttpStatus.CONFLICT)
public class UserAlreadyHasRoleException extends RuntimeException {
    public UserAlreadyHasRoleException(String message) {
        super(message);
    }
}
