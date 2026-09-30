package com.hospital.management.config;

import lombok.extern.slf4j.Slf4j;
import com.hospital.management.dto.ErrorResponseDTO;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.time.format.DateTimeParseException;

@Slf4j
@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(DateTimeParseException.class)
    public ResponseEntity<?> handleDateTimeParseException(DateTimeParseException ex) {
        log.error(ex.getMessage(), ex);
        return new ResponseEntity<>(new ErrorResponseDTO(ex.getMessage()), HttpStatus.BAD_REQUEST);
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<?> handleGenericException(Exception ex) {
        log.error(ex.getMessage(), ex);
        return new ResponseEntity<>(new ErrorResponseDTO("Error when processing your request"), HttpStatus.INTERNAL_SERVER_ERROR);
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ErrorResponseDTO> handleValidationException( MethodArgumentNotValidException ex) {
        log.error(ex.getMessage(), ex);
        String message = String.format("Validation Error: %s", ex.getBindingResult());
        return new ResponseEntity<>(new ErrorResponseDTO(message), HttpStatus.BAD_REQUEST);
    }

    @ExceptionHandler(HttpMessageNotReadableException.class)
    public ResponseEntity<ErrorResponseDTO> handleMalformedJson( HttpMessageNotReadableException ex) {
        log.error(ex.getMessage(), ex);
        return new ResponseEntity<>(new ErrorResponseDTO("Malformed request body"), HttpStatus.BAD_REQUEST);
    }

}
