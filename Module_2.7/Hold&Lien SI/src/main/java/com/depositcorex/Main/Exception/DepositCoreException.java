package com.depositcorex.Main.Exception;

/**
 * Base exception for the DepositCoreX system.
 * This allows you to catch all custom project errors in one block.
 */
public class DepositCoreException extends RuntimeException {
    public DepositCoreException(String message) {
        super(message);
    }
}