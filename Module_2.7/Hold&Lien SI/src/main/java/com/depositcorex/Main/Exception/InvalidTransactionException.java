package com.depositcorex.Main.Exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

/**
 * Thrown when business rules are violated (e.g., insufficient funds).
 * Returns HTTP 400 to the client.
 */
@ResponseStatus(HttpStatus.BAD_REQUEST)
public class InvalidTransactionException extends DepositCoreException {
    public InvalidTransactionException(String message) {
        super(message);
    }
}