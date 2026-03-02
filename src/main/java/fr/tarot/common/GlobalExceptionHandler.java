package fr.tarot.common;

import jakarta.servlet.http.HttpServletRequest;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.BindException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.*;

import java.time.Instant;

@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<ApiError> badRequest(IllegalArgumentException ex, HttpServletRequest req) {
        return build(HttpStatus.BAD_REQUEST, ex.getMessage(), req);
    }

    @ExceptionHandler({MethodArgumentNotValidException.class, BindException.class})
    public ResponseEntity<ApiError> validation(Exception ex, HttpServletRequest req) {
        return build(HttpStatus.BAD_REQUEST, "Validation invalide.", req);
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ApiError> internal(Exception ex, HttpServletRequest req) {
        return build(HttpStatus.INTERNAL_SERVER_ERROR, "Erreur interne.", req);
    }

    private ResponseEntity<ApiError> build(HttpStatus status, String msg, HttpServletRequest req) {
        ApiError err = new ApiError(
                Instant.now(),
                status.value(),
                status.getReasonPhrase(),
                msg,
                req.getRequestURI()
        );
        return ResponseEntity.status(status).body(err);
    }
}
