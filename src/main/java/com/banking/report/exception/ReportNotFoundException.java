package com.banking.report.exception;

/** Thrown when a report id does not exist. Maps to 404. */
public class ReportNotFoundException extends RuntimeException {

    public ReportNotFoundException(String message) {
        super(message);
    }
}
