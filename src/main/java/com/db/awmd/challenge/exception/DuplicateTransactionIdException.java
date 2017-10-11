package com.db.awmd.challenge.exception;

public class DuplicateTransactionIdException extends RuntimeException {

    public DuplicateTransactionIdException(String message) {
        super(message);
    }
}
