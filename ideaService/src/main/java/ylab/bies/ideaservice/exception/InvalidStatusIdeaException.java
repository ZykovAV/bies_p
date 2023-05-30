package ylab.bies.ideaservice.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(HttpStatus.NOT_FOUND)
public class InvalidStatusIdeaException extends RuntimeException{
    public InvalidStatusIdeaException(String message) {
        super(message);
    }

}
