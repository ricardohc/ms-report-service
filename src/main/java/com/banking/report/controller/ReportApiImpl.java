package com.banking.report.controller;

import com.banking.report.api.ReportApi;
import com.banking.report.document.ReportDocument;
import com.banking.report.dto.CardMovementsReportRequest;
import com.banking.report.dto.OperationAcceptedResponse;
import com.banking.report.dto.ProductReportRequest;
import com.banking.report.dto.ReportResponse;
import com.banking.report.service.ReportService;
import java.time.OffsetDateTime;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

/** REST adapter implementing the generated ReportApi contract. */
@RestController
@RequiredArgsConstructor
public class ReportApiImpl implements ReportApi {

    private final ReportService reportService;

    @Override
    public Mono<ResponseEntity<OperationAcceptedResponse>> generateProductReport(
            final String xCorrelationID, final String xChannelID, final UUID xTransactionID,
            final Mono<ProductReportRequest> productReportRequest,
            final ServerWebExchange exchange) {
        return productReportRequest
                .flatMap(req -> reportService.createProductReport(xTransactionID, req))
                .map(report -> accepted(report));
    }

    @Override
    public Mono<ResponseEntity<OperationAcceptedResponse>> generateCardMovementsReport(
            final String xCorrelationID, final String xChannelID, final UUID xTransactionID,
            final Mono<CardMovementsReportRequest> cardMovementsReportRequest,
            final ServerWebExchange exchange) {
        return cardMovementsReportRequest
                .flatMap(req -> reportService.createCardMovementsReport(xTransactionID, req))
                .map(report -> accepted(report));
    }

    @Override
    public Mono<ResponseEntity<ReportResponse>> findReportById(
            final UUID reportId, final String xCorrelationID, final String xChannelID,
            final ServerWebExchange exchange) {
        return reportService.findById(reportId)
                .map(report -> ResponseEntity.ok(toResponse(report)));
    }

    private static ResponseEntity<OperationAcceptedResponse> accepted(
            final ReportDocument report) {
        final OperationAcceptedResponse response = new OperationAcceptedResponse();
        response.setOperationId(UUID.randomUUID());
        response.setReportId(report.getReportId());
        response.setStatus(OperationAcceptedResponse.StatusEnum.ACCEPTED);
        return ResponseEntity.status(HttpStatus.ACCEPTED).body(response);
    }

    private static ReportResponse toResponse(final ReportDocument report) {
        final ReportResponse response = new ReportResponse();
        response.setReportId(report.getReportId());
        response.setStatus(ReportResponse.StatusEnum.valueOf(report.getStatus()));
        if (report.getGeneratedAt() != null) {
            response.setGeneratedAt(report.getGeneratedAt()
                    .atOffset(OffsetDateTime.now().getOffset()));
        }
        response.setDownloadUrl(report.getDownloadUrl());
        response.setErrorMessage(report.getErrorMessage());
        return response;
    }
}
