package com.library.library.service.exception;

public class AuthorAlreadyExistsException extends RuntimeException {

    public AuthorAlreadyExistsException(String message) {
        super(message);
    }
}
