package com.pepperkick.ems.server.util;

import com.pepperkick.ems.server.exception.BadRequestException;
import com.pepperkick.ems.server.exception.NotFoundException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;

@ControllerAdvice
public class ExceptionHelper {
    @ExceptionHandler(BadRequestException.class)
    public ResponseEntity badRequestException(BadRequestException exception) {
        return ResponseHelper.createErrorResponseEntity(exception.getMessage(), HttpStatus.BAD_REQUEST);
    }

    @ExceptionHandler(NotFoundException.class)
    public ResponseEntity notFoundException(NotFoundException exception) {
        return ResponseHelper.createErrorResponseEntity(exception.getMessage(), HttpStatus.NOT_FOUND);
    }
}
