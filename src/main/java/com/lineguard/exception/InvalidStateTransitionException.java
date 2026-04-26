package com.lineguard.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

/**
 * Thrown when an event cannot be applied to a production line in its current state.
 */
@ResponseStatus(HttpStatus.CONFLICT)
public class InvalidStateTransitionException extends RuntimeException {

    public InvalidStateTransitionException(String message) {
        super(message);
    }
}
