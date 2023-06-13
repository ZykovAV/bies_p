package ylab.bies.ideaservice.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(HttpStatus.FORBIDDEN)
public class VoteRejectedException extends RuntimeException {
    public VoteRejectedException(String message) {
        super(message);
    }
}
