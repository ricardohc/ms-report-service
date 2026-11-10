package com.banking.report.service.impl;

import com.banking.report.document.ReportDocument;
import com.banking.report.dto.CardMovementsReportRequest;
import com.banking.report.dto.ProductReportRequest;
import com.banking.report.event.ReportEventPublisher;
import com.banking.report.exception.InvalidDateRangeException;
import com.banking.report.exception.ReportNotFoundException;
import com.banking.report.repository.ReportRepository;
import com.banking.report.service.ReportService;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.time.ZoneOffset;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;

@Service
@RequiredArgsConstructor
@Slf4j
public class ReportServiceImpl implements ReportService {

    static final long MAX_RANGE_DAYS = 90;

    private final ReportRepository reportRepository;
    private final ReportEventPublisher eventPublisher;

    @Override
    public Mono<ReportDocument> createProductReport(final UUID transactionId,
            final ProductReportRequest request) {
        return Mono.defer(() -> {
            final LocalDate from = request.getFromDate();
            final LocalDate to = request.getToDate();
            if (from.isAfter(to)) {
                return Mono.error(new InvalidDateRangeException(
                        "fromDate must be less than or equal to toDate"));
            }
            final long range = ChronoUnit.DAYS.between(from, to);
            if (range > MAX_RANGE_DAYS) {
                return Mono.error(new InvalidDateRangeException(
                        "Date range must not exceed " + MAX_RANGE_DAYS + " days"));
            }
            final ReportDocument report = ReportDocument.builder()
                    .reportId(UUID.randomUUID())
                    .transactionId(transactionId)
                    .reportType("PRODUCT")
                    .productType(request.getProductType() != null
                            ? request.getProductType().name() : null)
                    .fromDate(toUtcStartOfDay(from))
                    .toDate(toUtcEndOfDay(to))
                    .format(request.getFormat() != null
                            ? request.getFormat().name() : "CSV")
                    .status("PENDING")
                    .build();
            return persistIdempotent(report)
                    .doOnNext(saved -> eventPublisher.publishReportRequested(saved));
        });
    }

    @Override
    public Mono<ReportDocument> createCardMovementsReport(final UUID transactionId,
            final CardMovementsReportRequest request) {
        return Mono.defer(() -> {
            final ReportDocument report = ReportDocument.builder()
                    .reportId(UUID.randomUUID())
                    .transactionId(transactionId)
                    .reportType("CARD_LAST_MOVEMENTS")
                    .cardType(request.getCardType() != null
                            ? request.getCardType().name() : "BOTH")
                    .customerId(request.getCustomerId())
                    .status("PENDING")
                    .build();
            return persistIdempotent(report)
                    .doOnNext(saved -> eventPublisher.publishReportRequested(saved));
        });
    }

    @Override
    public Mono<ReportDocument> findById(final UUID reportId) {
        return reportRepository.findById(reportId)
                .switchIfEmpty(Mono.error(
                        new ReportNotFoundException(
                                "Report not found: " + reportId)));
    }

    private Mono<ReportDocument> persistIdempotent(final ReportDocument report) {
        return reportRepository.findByTransactionId(report.getTransactionId())
                .flatMap(existing -> {
                    log.debug("Idempotent replay for transactionId={}",
                            report.getTransactionId());
                    return Mono.just(existing);
                })
                .switchIfEmpty(Mono.defer(() -> reportRepository.save(report)));
    }

    private static LocalDateTime toUtcStartOfDay(final LocalDate date) {
        return date.atStartOfDay(ZoneOffset.UTC).toLocalDateTime();
    }

    private static LocalDateTime toUtcEndOfDay(final LocalDate date) {
        return date.plusDays(1).atStartOfDay(ZoneOffset.UTC).toLocalDateTime()
                .minusNanos(1);
    }
}
