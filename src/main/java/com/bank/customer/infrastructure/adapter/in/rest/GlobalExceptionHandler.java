package com.bank.customer.infrastructure.adapter.in.rest;

import com.bank.customer.domain.exception.CustomerNotFoundException;
import com.bank.customer.domain.exception.DuplicateDocumentException;
import com.bank.customer.domain.exception.InvalidCustomerException;
import com.bank.customer.infrastructure.adapter.in.rest.dto.ErrorResponse;
import com.bank.customer.infrastructure.adapter.in.rest.dto.FieldError;
import java.time.OffsetDateTime;
import java.util.List;
import java.util.stream.Collectors;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.bind.support.WebExchangeBindException;
import org.springframework.web.server.ServerWebExchange;
import org.springframework.web.method.annotation.HandlerMethodValidationException;

/**
 * Translates domain/application exceptions and Bean Validation failures into the
 * standard ErrorResponse body (data-model.md section 4 / the ficha's error table).
 * InvalidCustomerException is ONE handler for all 4 business rules (422) — the specific
 * code comes from ex.getErrorCode(), set where each rule is enforced in the aggregate.
 */
@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(CustomerNotFoundException.class)
    public ResponseEntity<ErrorResponse> handleNotFound(CustomerNotFoundException ex, ServerWebExchange exchange) {
        return build(HttpStatus.NOT_FOUND, "CUSTOMER_NOT_FOUND", ex.getMessage(), exchange, null);
    }

    @ExceptionHandler(DuplicateDocumentException.class)
    public ResponseEntity<ErrorResponse> handleDuplicate(DuplicateDocumentException ex, ServerWebExchange exchange) {
        return build(HttpStatus.CONFLICT, "DOCUMENT_ALREADY_REGISTERED", ex.getMessage(), exchange, null);
    }

    @ExceptionHandler(InvalidCustomerException.class)
    public ResponseEntity<ErrorResponse> handleInvalid(InvalidCustomerException ex, ServerWebExchange exchange) {
        return build(HttpStatus.UNPROCESSABLE_ENTITY, ex.getErrorCode(), ex.getMessage(), exchange, null);
    }

    /** Body validation failures: @Valid @RequestBody Mono&lt;X&gt; on create/update/changeProfile. */
    @ExceptionHandler(WebExchangeBindException.class)
    public ResponseEntity<ErrorResponse> handleBodyValidation(WebExchangeBindException ex,
                                                                ServerWebExchange exchange) {
        List<FieldError> details = ex.getFieldErrors().stream()
                .map(fieldError -> {
                    FieldError detail = new FieldError();
                    detail.setField(fieldError.getField());
                    detail.setMessage(fieldError.getDefaultMessage());
                    return detail;
                })
                .collect(Collectors.toList());
        return build(HttpStatus.BAD_REQUEST, "VALIDATION_ERROR", "Validation failed", exchange, details);
    }

    /**
     * Query/path param validation failures (e.g. documentNumber's @Pattern). Spring 6.1+
     * (Boot 3.5) reports these as HandlerMethodValidationException. If your stack trace
     * shows a different type here (some setups still see ConstraintViolationException),
     * tell me and I adjust this handler — don't guess a fix yourself.
     */
    @ExceptionHandler(HandlerMethodValidationException.class)
    public ResponseEntity<ErrorResponse> handleParamValidation(HandlerMethodValidationException ex,
                                                                 ServerWebExchange exchange) {
        return build(HttpStatus.BAD_REQUEST, "VALIDATION_ERROR", ex.getReason(), exchange, null);
    }

    private ResponseEntity<ErrorResponse> build(HttpStatus status, String code, String message,
                                                  ServerWebExchange exchange, List<FieldError> details) {
        ErrorResponse body = new ErrorResponse();
        body.setTimestamp(OffsetDateTime.now());
        body.setStatus(status.value());
        body.setCode(code);
        body.setMessage(message);
        body.setPath(exchange.getRequest().getPath().value());
        body.setDetails(details);
        return ResponseEntity.status(status).body(body);
    }
}
