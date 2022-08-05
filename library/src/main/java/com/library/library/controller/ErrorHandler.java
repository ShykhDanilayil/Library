package com.library.library.controller;

import com.library.library.service.exception.BookNotAvailableException;
import com.library.library.service.exception.BorrowedException;
import com.library.library.service.exception.EntityNotFoundException;
import com.library.library.service.exception.ReservedException;
import com.library.library.service.exception.UserAlreadyExistsException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import javax.validation.ConstraintViolationException;
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

    @ExceptionHandler(ConstraintViolationException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public Error handleConstraintViolationException(ConstraintViolationException ex) {
        log.error("handleConstraintViolationException: exception {}", ex.getMessage(), ex);
        return new Error(ex.getMessage());
    }

    @ExceptionHandler(EntityNotFoundException.class)
    @ResponseStatus(HttpStatus.NOT_FOUND)
    public Error handleEntityNotFoundException(EntityNotFoundException ex) {
        log.error("handleEntityNotFoundException: exception {}", ex.getMessage(), ex);
        return new Error(ex.getMessage());
    }

    @ExceptionHandler(UserAlreadyExistsException.class)
    @ResponseStatus(HttpStatus.NOT_FOUND)
    public Error handleUserAlreadyExistsException(UserAlreadyExistsException ex) {
        log.error("handleUserAlreadyExistsException: exception {}", ex.getMessage(), ex);
        return new Error(ex.getMessage());
    }

    @ExceptionHandler(BookNotAvailableException.class)
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public Error handleBookNotAvailableException(BookNotAvailableException ex) {
        log.error("handleBookNotAvailableException: exception {}", ex.getMessage(), ex);
        return new Error(ex.getMessage());
    }

    @ExceptionHandler(ReservedException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public Error handleReservedException(ReservedException ex) {
        log.error("handleReservedException: exception {}", ex.getMessage(), ex);
        return new Error(ex.getMessage());
    }

    @ExceptionHandler(BorrowedException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public Error handleBorrowedException(BorrowedException ex) {
        log.error("handleBorrowedException: exception {}", ex.getMessage(), ex);
        return new Error(ex.getMessage());
    }
}

