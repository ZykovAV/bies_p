package ylab.bies.ideaservice.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(HttpStatus.NOT_MODIFIED)
public class VoteRejectedException extends RuntimeException {
    public VoteRejectedException(String message) {
        super(message);
    }
}
