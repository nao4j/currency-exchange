package com.nao4j.currencyexchange.validation;

import org.springframework.http.HttpStatus;
import org.springframework.ui.ModelMap;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.context.request.ServletWebRequest;

import javax.validation.ConstraintViolationException;

import static java.time.ZonedDateTime.now;
import static java.util.stream.Collectors.joining;
import static org.springframework.http.HttpStatus.BAD_REQUEST;

@ControllerAdvice
public class ValidationExceptionHandler {

    private static final String ERROR_MESSAGE_DELIMITER = "; ";

    @ResponseBody
    @ResponseStatus(BAD_REQUEST)
    @ExceptionHandler(ConstraintViolationException.class)
    public ModelMap handle(final ServletWebRequest request, final ConstraintViolationException e) {
        final var message = e.getConstraintViolations().stream()
                .map(value -> value.getPropertyPath() + ": " + value.getMessage())
                .collect(joining(ERROR_MESSAGE_DELIMITER));
        return body(request, BAD_REQUEST, message);
    }

    @ResponseBody
    @ResponseStatus(BAD_REQUEST)
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ModelMap handle(final ServletWebRequest request, final MethodArgumentNotValidException e) {
        final var message = e.getBindingResult().getFieldErrors().stream()
                .map(value -> value.getField() + ": " + value.getDefaultMessage())
                .collect(joining(ERROR_MESSAGE_DELIMITER));
        return body(request, BAD_REQUEST, message);
    }

    private ModelMap body(final ServletWebRequest request, final HttpStatus status, final String message) {
        final var body = new ModelMap();
        body.put("timestamp", now());
        body.put("status", status.value());
        body.put("error", status.getReasonPhrase());
        body.put("message", message);
        body.put("path", request.getRequest().getRequestURI());
        return body;
    }

}
