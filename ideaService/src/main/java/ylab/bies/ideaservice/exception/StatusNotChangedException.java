package ylab.bies.ideaservice.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(HttpStatus.NOT_MODIFIED)
public class StatusNotChangedException extends RuntimeException {
    public StatusNotChangedException(String message) {
        super(message);
    }
}
