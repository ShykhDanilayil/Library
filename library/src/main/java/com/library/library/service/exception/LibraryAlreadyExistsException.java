package com.library.library.service.exception;

public class LibraryAlreadyExistsException extends RuntimeException {

    public LibraryAlreadyExistsException(String message) {
        super(message);
    }
}
