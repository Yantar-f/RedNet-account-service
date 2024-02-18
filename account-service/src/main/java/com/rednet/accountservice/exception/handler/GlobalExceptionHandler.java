package com.rednet.accountservice.exception.handler;

import com.rednet.accountservice.exception.AccountNotFoundException;
import com.rednet.accountservice.exception.InvalidTokenException;
import com.rednet.accountservice.exception.OccupiedValueException;
import com.rednet.accountservice.exception.ServerErrorException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.ConstraintViolationException;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.ProblemDetail;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.http.converter.HttpMessageNotWritableException;
import org.springframework.lang.NonNull;
import org.springframework.validation.FieldError;
import org.springframework.web.HttpMediaTypeNotAcceptableException;
import org.springframework.web.HttpMediaTypeNotSupportedException;
import org.springframework.web.HttpRequestMethodNotSupportedException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.MissingServletRequestParameterException;
import org.springframework.web.bind.ServletRequestBindingException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.context.request.ServletWebRequest;
import org.springframework.web.context.request.WebRequest;
import org.springframework.web.context.request.async.AsyncRequestTimeoutException;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;
import org.springframework.web.multipart.support.MissingServletRequestPartException;
import org.springframework.web.servlet.NoHandlerFoundException;
import org.springframework.web.servlet.mvc.method.annotation.ResponseEntityExceptionHandler;

import java.net.URI;

import static org.springframework.http.HttpStatus.BAD_REQUEST;
import static org.springframework.http.HttpStatus.CONFLICT;
import static org.springframework.http.HttpStatus.INTERNAL_SERVER_ERROR;
import static org.springframework.http.HttpStatus.NOT_FOUND;
import static org.springframework.http.HttpStatus.SERVICE_UNAVAILABLE;
import static org.springframework.http.MediaType.APPLICATION_PROBLEM_JSON;

@RestControllerAdvice
@Order(Ordered.HIGHEST_PRECEDENCE)
public class GlobalExceptionHandler extends ResponseEntityExceptionHandler {
    @Override
    protected ResponseEntity<Object> handleHttpRequestMethodNotSupported(
            @NonNull HttpRequestMethodNotSupportedException ex,
            @NonNull HttpHeaders headers,
            @NonNull HttpStatusCode status,
            @NonNull WebRequest request) {
        return generateErrorResponse(
                BAD_REQUEST,
                extractURI(request),
                "Method not supported",
                ex.getMessage());
    }

    @Override
    protected ResponseEntity<Object> handleHttpMediaTypeNotSupported(
            @NonNull HttpMediaTypeNotSupportedException ex,
            @NonNull HttpHeaders headers,
            @NonNull HttpStatusCode status,
            @NonNull WebRequest request) {
        return generateErrorResponse(
                BAD_REQUEST,
                extractURI(request),
                "Media type not supported",
                ex.getMessage()
        );
    }

    @Override
    protected ResponseEntity<Object> handleHttpMediaTypeNotAcceptable(
            @NonNull HttpMediaTypeNotAcceptableException ex,
            @NonNull HttpHeaders headers,
            @NonNull HttpStatusCode status,
            @NonNull WebRequest request) {
        return generateErrorResponse(
                BAD_REQUEST,
                extractURI(request),
                "Not acceptable media type",
                ex.getMessage()
        );
    }

    @Override
    protected ResponseEntity<Object> handleMissingServletRequestParameter(
            @NonNull MissingServletRequestParameterException ex,
            @NonNull HttpHeaders headers,
            @NonNull HttpStatusCode status,
            @NonNull WebRequest request) {
        return generateErrorResponse(
                BAD_REQUEST,
                extractURI(request),
                "Missing request param",
                ex.getMessage()
        );
    }

    @Override
    protected ResponseEntity<Object> handleMissingServletRequestPart(
            @NonNull MissingServletRequestPartException ex,
            @NonNull HttpHeaders headers,
            @NonNull HttpStatusCode status,
            @NonNull WebRequest request) {
        return generateErrorResponse(
                BAD_REQUEST,
                extractURI(request),
                "Missing request part",
                ex.getMessage()
        );
    }

    @Override
    protected ResponseEntity<Object> handleServletRequestBindingException(
            @NonNull ServletRequestBindingException ex,
            @NonNull HttpHeaders headers,
            @NonNull HttpStatusCode status,
            @NonNull WebRequest request) {
        return generateErrorResponse(
                BAD_REQUEST,
                extractURI(request),
                "Binding exception",
                ex.getMessage()
        );
    }

    @Override
    protected ResponseEntity<Object> handleMethodArgumentNotValid(
            @NonNull MethodArgumentNotValidException ex,
            @NonNull HttpHeaders headers,
            @NonNull HttpStatusCode status,
            @NonNull WebRequest request) {
        FieldError fieldError = ex.getBindingResult().getFieldError();
        String errorMessage = fieldError != null ? fieldError.getDefaultMessage() : "undefined constraint violation";

        return generateErrorResponse(
                BAD_REQUEST,
                extractURI(request),
                "Argument nor valid",
                errorMessage
        );
    }

    @Override
    protected ResponseEntity<Object> handleNoHandlerFoundException(
            @NonNull NoHandlerFoundException ex,
            @NonNull HttpHeaders headers,
            @NonNull HttpStatusCode status,
            @NonNull WebRequest request) {
        return generateErrorResponse(
                NOT_FOUND,
                extractURI(request),
                "No handler found",
                ex.getMessage()
        );
    }

    @Override
    protected ResponseEntity<Object> handleAsyncRequestTimeoutException(
            @NonNull AsyncRequestTimeoutException ex,
            @NonNull HttpHeaders headers,
            @NonNull HttpStatusCode status,
            @NonNull WebRequest request) {
        return generateErrorResponse(
                SERVICE_UNAVAILABLE,
                extractURI(request),
                "Request timeout",
                ex.getMessage()
        );
    }

    @Override
    protected ResponseEntity<Object> handleHttpMessageNotWritable(
            @NonNull HttpMessageNotWritableException ex,
            @NonNull HttpHeaders headers,
            @NonNull HttpStatusCode status,
            @NonNull WebRequest request) {
        return generateErrorResponse(
                INTERNAL_SERVER_ERROR,
                extractURI(request),
                "Not writable http message",
                ex.getMessage()
        );
    }

    @Override
    protected ResponseEntity<Object> handleHttpMessageNotReadable(
            @NonNull HttpMessageNotReadableException ex,
            @NonNull HttpHeaders headers,
            @NonNull HttpStatusCode status,
            @NonNull WebRequest request) {
        return generateErrorResponse(
                BAD_REQUEST,
                extractURI(request),
                "Not readable http message",
                ex.getMessage()
        );
    }

    @ExceptionHandler(MethodArgumentTypeMismatchException.class)
    protected ResponseEntity<Object> handleMethodArgumentTypeMismatch(MethodArgumentTypeMismatchException ex,
                                                                      WebRequest request) {
        return generateErrorResponse(
                BAD_REQUEST,
                extractURI(request),
                "Argument type mismatch",
                ex.getMessage()
        );
    }

    @ExceptionHandler(ConstraintViolationException.class)
    protected ResponseEntity<Object> handleConstraintViolationException(ConstraintViolationException ex,
                                                                        WebRequest request) {
        return generateErrorResponse(
                BAD_REQUEST,
                extractURI(request),
                "Constraint violation",
                ex.getMessage()
        );
    }

    @ExceptionHandler(InvalidTokenException.class)
    protected ResponseEntity<Object> handleInvalidToken(InvalidTokenException ex,
                                                        HttpServletRequest request) {
        return generateErrorResponse(
                BAD_REQUEST,
                request.getRequestURI(),
                "Invalid token",
                ex.getMessage()
        );
    }

    @ExceptionHandler(AccountNotFoundException.class)
    protected ResponseEntity<Object> handleAccountNotFound(AccountNotFoundException ex,
                                                           HttpServletRequest request) {
        return generateErrorResponse(
                NOT_FOUND,
                request.getRequestURI(),
                "Account not found",
                ex.getMessage()
        );
    }

    @ExceptionHandler(OccupiedValueException.class)
    protected ResponseEntity<Object> handleOccupiedValues(OccupiedValueException ex,
                                                          HttpServletRequest request) {
        return generateErrorResponse(
                CONFLICT,
                request.getRequestURI(),
                "Occupied values",
                ex.getMessage()
        );
    }

    @ExceptionHandler(value = ServerErrorException.class)
    protected ResponseEntity<Object> handleServerError(ServerErrorException ex,
                                                       HttpServletRequest request) {
        return generateErrorResponse(
                INTERNAL_SERVER_ERROR,
                request.getRequestURI(),
                "Internal server error",
                ex.getMessage()
        );
    }

    private ResponseEntity<Object> generateErrorResponse(HttpStatus httpStatus,
                                                         String requestUri,
                                                         String errorTitle,
                                                         String errorMessage) {

        ProblemDetail body = ProblemDetail.forStatus(httpStatus.value());

        body.setDetail(errorMessage);
        body.setInstance(URI.create(requestUri));
        body.setTitle(errorTitle);

        return ResponseEntity
                .status(httpStatus.value())
                .contentType(APPLICATION_PROBLEM_JSON)
                .body(body);
    }

    private String extractURI(WebRequest request) {
        if (request instanceof ServletWebRequest webRequest) {
            return webRequest.getRequest().getRequestURI();
        } else {
            return "";
        }
    }
}
