package com.library.library.controller.validation;

import com.library.library.service.exception.AuthorAlreadyExistsException;
import com.library.library.service.exception.EntityNotFoundException;
import com.library.library.service.exception.LibraryAlreadyExistsException;
import com.library.library.service.exception.UserAlreadyExistsException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@RestControllerAdvice
public class ErrorHandler {

    @ExceptionHandler(MethodArgumentNotValidException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public List<Error> handleMethodArgumentNotValidException(MethodArgumentNotValidException ex) {
        log.error("handleMethodArgumentNotValidException: exception {}", ex.getMessage(), ex);
        return ex.getBindingResult().getAllErrors().stream()
                .map(err -> new Error(err.getDefaultMessage()))
                .collect(Collectors.toList());
    }

    @ExceptionHandler(EntityNotFoundException.class)
    @ResponseStatus(HttpStatus.NOT_FOUND)
    public Error handleEntityNotFoundException(EntityNotFoundException ex) {
        log.error("handleEntityNotFoundException: exception {}", ex.getMessage(), ex);
        return new Error(ex.getMessage());
    }

    @ExceptionHandler(AuthorAlreadyExistsException.class)
    @ResponseStatus(HttpStatus.NOT_FOUND)
    public Error handleAuthorAlreadyExistsException(AuthorAlreadyExistsException ex) {
        log.error("handleAuthorAlreadyExistsException: exception {}", ex.getMessage(), ex);
        return new Error(ex.getMessage());
    }

    @ExceptionHandler(LibraryAlreadyExistsException.class)
    @ResponseStatus(HttpStatus.NOT_FOUND)
    public Error handleLibraryAlreadyExistsException(LibraryAlreadyExistsException ex) {
        log.error("handleLibraryAlreadyExistsException: exception {}", ex.getMessage(), ex);
        return new Error(ex.getMessage());
    }

    @ExceptionHandler(UserAlreadyExistsException.class)
    @ResponseStatus(HttpStatus.NOT_FOUND)
    public Error handleUserAlreadyExistsException(UserAlreadyExistsException ex) {
        log.error("handleUserAlreadyExistsException: exception {}", ex.getMessage(), ex);
        return new Error(ex.getMessage());
    }

    @ExceptionHandler(NullPointerException.class)
    @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
    public String handleNullPointerException(NullPointerException ex) {
        return ex.getMessage();
    }

}

