package se.iths.martin.productserviceprojekt2.exception;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

/**
 * Översätter produkt- och valideringsfel till HTTP-svar med felinformation.
 */
@RestControllerAdvice
public class GlobalExceptionHandler {
    /**
     * Returnerar HTTP 404 när produkten saknas.
     *
     * @param ex felet som ska översättas till ett HTTP-svar
     * @return HTTP-svar med statuskod och eventuell svarskropp
     */
    @ExceptionHandler(ProductNotFoundException.class)
    public ResponseEntity<Map<String, Object>> handleProductNotFound(ProductNotFoundException ex) {
        return buildError(HttpStatus.NOT_FOUND, ex.getMessage());
    }

    /**
     * Returnerar HTTP 409 när lagret inte räcker.
     *
     * @param ex felet som ska översättas till ett HTTP-svar
     * @return HTTP-svar med statuskod och eventuell svarskropp
     */
    @ExceptionHandler(InsufficientStockException.class)
    public ResponseEntity<Map<String, Object>> handleInsufficientStock(InsufficientStockException ex) {
        return buildError(HttpStatus.CONFLICT, ex.getMessage());
    }

    /**
     * Returnerar HTTP 403 när åtkomst nekas.
     *
     * @param ex felet som ska översättas till ett HTTP-svar
     * @return HTTP-svar med statuskod och eventuell svarskropp
     */
    @ExceptionHandler(AccessDeniedException.class)
    public ResponseEntity<Map<String, Object>> handleAccessDenied(AccessDeniedException ex) {
        return buildError(HttpStatus.FORBIDDEN, ex.getMessage());
    }

    /**
     * Returnerar HTTP 400 med information om ogiltiga fält.
     *
     * @param ex felet som ska översättas till ett HTTP-svar
     * @return HTTP-svar med statuskod och eventuell svarskropp
     */
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<Map<String, Object>> handleValidation(MethodArgumentNotValidException ex) {
        String message = ex.getBindingResult().getFieldErrors().stream()
                .map(error -> error.getField() + ": " + error.getDefaultMessage())
                .reduce((a, b) -> a + ", " + b)
                .orElse("Validation failed");
        return buildError(HttpStatus.BAD_REQUEST, message);
    }

    /**
     * Returnerar HTTP 500 för oväntade fel.
     *
     * @param ex felet som ska översättas till ett HTTP-svar
     * @return HTTP-svar med statuskod och eventuell svarskropp
     */
    @ExceptionHandler(Exception.class)
    public ResponseEntity<Map<String, Object>> handleGeneric(Exception ex) {
        return buildError(HttpStatus.INTERNAL_SERVER_ERROR, ex.getMessage());
    }

    private ResponseEntity<Map<String, Object>> buildError(HttpStatus status, String message) {
        Map<String, Object> body = new HashMap<>();
        body.put("timestamp", LocalDateTime.now().toString());
        body.put("status", status.value());
        body.put("error", status.getReasonPhrase());
        body.put("message", message);
        return ResponseEntity.status(status).body(body);
    }
}
