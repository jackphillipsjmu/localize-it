package com.example.microservice.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

/**
 * Exception class that will pass along the 404 HTTP Status code up the chain to
 * clients hitting REST endpoints. There's are more than one way to do this but
 * this provides one example implementation. Also, other Spring based error handlers
 * overwrite the 404 status so make sure to test thoroughly in your application!
 *
 * Annotation Explained:
 *
 * <ul>
 *     <li>
 *         ResponseStatus = Marks a method or exception class with the status code
 *         and reason that should be returned.
 *     </li>
 * </ul>
 *
 * @author Jack Phillips
 */
@ResponseStatus(value = HttpStatus.NOT_FOUND)
public class NotFoundException extends RuntimeException {

    /**
     * Default no-arg Constructor
     */
    public NotFoundException() {
        this("Not found Exception Invoked");
    }

    /**
     * Constructs a Runtime Service Exception to be used when something is not found
     * in the underlying application.
     *
     * @param message String the error message to append to exception
     */
    public NotFoundException(String message) {
        super(message);
    }
}
