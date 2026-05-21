package com.depositcorex.Main.Exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

/**
 * Thrown when a requested resource (Account, Hold, SI) is missing.
 * Returns HTTP 404 to the client.
 */
@ResponseStatus(HttpStatus.NOT_FOUND)
public class ResourceNotFoundException extends DepositCoreException {
    public ResourceNotFoundException(String message) {
        super(message);
    }
}