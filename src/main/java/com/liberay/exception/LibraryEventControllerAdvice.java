package com.liberay.exception;


import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;

import java.util.List;
import java.util.stream.Collectors;

@ControllerAdvice
@Slf4j
public class LibraryEventControllerAdvice {

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<?> exceptionHandler(MethodArgumentNotValidException ex){

        List<FieldError> fieldError = ex.getBindingResult().getFieldErrors();

        String errorMessage = fieldError.stream()
                .map(field -> field.getField() + " - " + field.getDefaultMessage())
                .sorted()
                .collect(Collectors.joining(","));

        log.info("ErrorMessage: {}", errorMessage );

        return new ResponseEntity<>(errorMessage, HttpStatus.BAD_REQUEST);

    }
}
