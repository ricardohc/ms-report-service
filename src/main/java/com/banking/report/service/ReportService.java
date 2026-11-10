package com.banking.report.service;

import com.banking.report.document.ReportDocument;
import com.banking.report.dto.CardMovementsReportRequest;
import com.banking.report.dto.ProductReportRequest;
import java.util.UUID;
import reactor.core.publisher.Mono;

public interface ReportService {

    Mono<ReportDocument> createProductReport(UUID transactionId, ProductReportRequest request);

    Mono<ReportDocument> createCardMovementsReport(UUID transactionId,
            CardMovementsReportRequest request);

    Mono<ReportDocument> findById(UUID reportId);
}
