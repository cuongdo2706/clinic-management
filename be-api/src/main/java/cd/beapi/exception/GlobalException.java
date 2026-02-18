package cd.beapi.exception;

import cd.beapi.dto.response.ErrorResponse;
import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.MalformedJwtException;
import io.jsonwebtoken.UnsupportedJwtException;
import jakarta.validation.ConstraintViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.context.request.WebRequest;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;

import java.io.IOException;
import java.time.Instant;
import java.util.Objects;

@RestControllerAdvice
public class GlobalException {
    @ExceptionHandler(MethodArgumentNotValidException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public ErrorResponse handleValidation(MethodArgumentNotValidException ex, WebRequest request) {
        return new ErrorResponse(
                Instant.now(),
                HttpStatus.BAD_REQUEST.value(),
                request.getDescription(false).replace("uri=", ""),
                HttpStatus.BAD_REQUEST.getReasonPhrase(),
                Objects.requireNonNull(ex.getFieldError()).getDefaultMessage());
    }

    @ExceptionHandler(ConstraintViolationException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public ErrorResponse handlePathVariable(ConstraintViolationException ex, WebRequest request) {
        return new ErrorResponse(
                Instant.now(),
                HttpStatus.BAD_REQUEST.value(),
                request.getDescription(false).replace("uri=", ""),
                "Path Variable Invalid",
                Objects.requireNonNull(ex.getMessage())
        );
    }

    @ExceptionHandler(MethodArgumentTypeMismatchException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public ErrorResponse handleConvert(MethodArgumentTypeMismatchException ex, WebRequest request) {
        return new ErrorResponse(
                Instant.now(),
                HttpStatus.BAD_REQUEST.value(),
                request.getDescription(false).replace("uri=", ""),
                "Failed to convert value of type",
                ex.getMessage()
        );
    }

    @ExceptionHandler(DataExistedException.class)
    @ResponseStatus(HttpStatus.CONFLICT)
    public ErrorResponse handleDataExisted(DataExistedException ex, WebRequest request) {
        return new ErrorResponse(
                Instant.now(),
                HttpStatus.CONFLICT.value(),
                request.getDescription(false).replace("uri=", ""),
                "Data Existed",
                ex.getMessage()
        );
    }

    @ExceptionHandler(DataNotFoundException.class)
    @ResponseStatus(HttpStatus.NOT_FOUND)
    public ErrorResponse handleDataNotFound(DataNotFoundException ex, WebRequest request) {
        return new ErrorResponse(
                Instant.now(),
                HttpStatus.NOT_FOUND.value(),
                request.getDescription(false).replace("uri=", ""),
                "Data Not Found",
                ex.getMessage()
        );
    }

    @ExceptionHandler(IOException.class)
    @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
    public ErrorResponse handleIOException(IOException ex, WebRequest request) {
        return new ErrorResponse(
                Instant.now(),
                HttpStatus.NOT_FOUND.value(),
                request.getDescription(false).replace("uri=", ""),
                "In/Out Error",
                ex.getMessage()
        );
    }

    @ExceptionHandler(MalformedJwtException.class)
    @ResponseStatus(HttpStatus.UNAUTHORIZED)
    public ErrorResponse handleMalformedJwtException(MalformedJwtException ex, WebRequest request) {
        return new ErrorResponse(
                Instant.now(),
                HttpStatus.UNAUTHORIZED.value(),
                request.getDescription(false).replace("uri=", ""),
                "Invalid JWT Token",
                ex.getMessage()
        );
    }

    @ExceptionHandler(ExpiredJwtException.class)
    @ResponseStatus(HttpStatus.UNAUTHORIZED)
    public ErrorResponse handleExpiredJwtException(ExpiredJwtException ex, WebRequest request) {
        return new ErrorResponse(
                Instant.now(),
                HttpStatus.UNAUTHORIZED.value(),
                request.getDescription(false).replace("uri=", ""),
                "Expired JWT Token",
                ex.getMessage()
        );
    }

    @ExceptionHandler(UnsupportedJwtException.class)
    @ResponseStatus(HttpStatus.UNAUTHORIZED)
    public ErrorResponse handleUnsupportedJwtException(UnsupportedJwtException ex, WebRequest request) {
        return new ErrorResponse(
                Instant.now(),
                HttpStatus.UNAUTHORIZED.value(),
                request.getDescription(false).replace("uri=", ""),
                "Unsupported JWT Token",
                ex.getMessage()
        );
    }

    @ExceptionHandler(IllegalArgumentException.class)
    @ResponseStatus(HttpStatus.UNAUTHORIZED)
    public ErrorResponse handleIllegalArgumentException(IllegalArgumentException ex, WebRequest request) {
        return new ErrorResponse(
                Instant.now(),
                HttpStatus.UNAUTHORIZED.value(),
                request.getDescription(false).replace("uri=", ""),
                "JWT claims string is empty",
                ex.getMessage()
        );
    }

    @ExceptionHandler(DataConflictException.class)
    @ResponseStatus(HttpStatus.CONFLICT)
    public ErrorResponse handleDataConflictException(DataConflictException ex, WebRequest request) {
        return new ErrorResponse(
                Instant.now(),
                HttpStatus.CONFLICT.value(),
                request.getDescription(false).replace("uri=", ""),
                "Data conflicted",
                ex.getMessage()
        );
    }
}
