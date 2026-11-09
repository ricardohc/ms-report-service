package com.banking.report.service;

import com.banking.report.document.ReportDocument;
import com.banking.report.dto.CardMovementsReportRequest;
import com.banking.report.dto.ProductReportRequest;
import java.util.UUID;
import reactor.core.publisher.Mono;

/** Report business logic. */
public interface ReportService {

    /**
     * Creates (or returns the existing, idempotent) product report request.
     *
     * @param transactionId X-Transaction-ID header for idempotency
     * @param request       validated request body
     * @return the report document (PENDING)
     */
    Mono<ReportDocument> createProductReport(UUID transactionId, ProductReportRequest request);

    /**
     * Creates (or returns the existing, idempotent) card movements report request.
     *
     * @param transactionId X-Transaction-ID header for idempotency
     * @param request       validated request body
     * @return the report document (PENDING)
     */
    Mono<ReportDocument> createCardMovementsReport(UUID transactionId,
            CardMovementsReportRequest request);

    /** Finds a report by id or errors with ReportNotFoundException. */
    Mono<ReportDocument> findById(UUID reportId);
}
