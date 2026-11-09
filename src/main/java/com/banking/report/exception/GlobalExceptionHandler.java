package com.banking.report.exception;

import com.banking.report.dto.ErrorResponse;
import java.time.OffsetDateTime;
import java.util.List;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.server.ServerWebExchange;

/** Global error handler mapping domain exceptions to contract HTTP codes. */
@RestControllerAdvice
@Slf4j
public class GlobalExceptionHandler {

    /** 422 - invalid date range. */
    @ExceptionHandler(InvalidDateRangeException.class)
    public ResponseEntity<ErrorResponse> handleInvalidDateRange(
            final InvalidDateRangeException ex, final ServerWebExchange exchange) {
        return build(HttpStatus.UNPROCESSABLE_ENTITY, "INVALID_DATE_RANGE",
                ex.getMessage(), exchange);
    }

    /** 404 - report not found. */
    @ExceptionHandler(ReportNotFoundException.class)
    public ResponseEntity<ErrorResponse> handleNotFound(
            final ReportNotFoundException ex, final ServerWebExchange exchange) {
        return build(HttpStatus.NOT_FOUND, "REPORT_NOT_FOUND",
                ex.getMessage(), exchange);
    }

    /** 500 - unexpected errors. */
    @ExceptionHandler(Exception.class)
    public ResponseEntity<ErrorResponse> handleGeneric(
            final Exception ex, final ServerWebExchange exchange) {
        log.error("Unhandled error", ex);
        return build(HttpStatus.INTERNAL_SERVER_ERROR, "INTERNAL_ERROR",
                "Unexpected error", exchange);
    }

    private ResponseEntity<ErrorResponse> build(final HttpStatus status, final String code,
            final String message, final ServerWebExchange exchange) {
        final ErrorResponse error = new ErrorResponse();
        error.setCode(code);
        error.setMessage(message);
        error.setTimestamp(OffsetDateTime.now());
        error.setCorrelationId(exchange.getRequest().getHeaders()
                .getFirst("X-Correlation-ID"));
        error.setDetails(List.of());
        return ResponseEntity.status(status).body(error);
    }
}
