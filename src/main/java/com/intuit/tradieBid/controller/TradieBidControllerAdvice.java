package com.intuit.tradieBid.controller;

import com.intuit.tradieBid.exception.InvalidBidException;
import com.intuit.tradieBid.exception.NoMoreBidException;
import com.intuit.tradieBid.exception.NoWinningBidException;
import com.intuit.tradieBid.exception.NotFoundException;
import org.apache.commons.lang3.StringUtils;
import org.postgresql.util.PSQLException;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.context.request.WebRequest;
import org.springframework.web.servlet.mvc.method.annotation.ResponseEntityExceptionHandler;

@ControllerAdvice
public class TradieBidControllerAdvice extends ResponseEntityExceptionHandler {

    @ExceptionHandler(value = {PSQLException.class, DataIntegrityViolationException.class})
    protected ResponseEntity<Object> handlePSQLException(
            PSQLException ex, WebRequest request) {
        if (ex.getMessage().contains("violates not-null constraint"))
            return handleExceptionInternal(ex, StringUtils.substringBefore(StringUtils.substringAfter(ex.getMessage(), "column "), " ") + " is required.",
                    new HttpHeaders(), HttpStatus.BAD_REQUEST, request);
        else if (ex.getMessage().contains("could not execute statement; SQL [n/a]; constraint"))
            return handleExceptionInternal(ex, StringUtils.substringBefore(StringUtils.substringAfter(ex.getMessage(), "constraint ["), "\"") + " is required.",
                    new HttpHeaders(), HttpStatus.BAD_REQUEST, request);
        else return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
    }

    @ExceptionHandler(value = {RuntimeException.class})
    protected ResponseEntity<Object> handleInternalException(RuntimeException ex, WebRequest request) {
        if (ex instanceof NoWinningBidException)
            return handleExceptionInternal(ex, "No Winning bid available", new HttpHeaders(), HttpStatus.UNPROCESSABLE_ENTITY, request);
        else if (ex instanceof NotFoundException)
            return handleExceptionInternal(ex, "Not Found", new HttpHeaders(), HttpStatus.NOT_FOUND, request);
        else if (ex instanceof NoMoreBidException)
            return handleExceptionInternal(ex, "Not More Bid Allowed", new HttpHeaders(), HttpStatus.BAD_REQUEST, request);
        else if (ex instanceof InvalidBidException)
            return handleExceptionInternal(ex, "Bid should have either fix price or hourly rate", new HttpHeaders(), HttpStatus.BAD_REQUEST, request);
        else return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
    }
}
