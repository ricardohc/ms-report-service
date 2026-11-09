package com.banking.report.exception;

/** Thrown when fromDate > toDate or the range exceeds 90 days. Maps to 422. */
public class InvalidDateRangeException extends RuntimeException {

    public InvalidDateRangeException(String message) {
        super(message);
    }
}
