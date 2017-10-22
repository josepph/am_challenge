package com.db.awmd.challenge.exception;

public class DatabaseErrorException extends RuntimeException {

  public DatabaseErrorException(String message) {
        super(message);
    }
}
