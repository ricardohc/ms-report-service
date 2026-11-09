package com.banking.report.service.impl;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.when;

import com.banking.report.document.ReportDocument;
import com.banking.report.dto.CardMovementsReportRequest;
import com.banking.report.dto.ProductReportRequest;
import com.banking.report.dto.ProductType;
import com.banking.report.event.ReportEventPublisher;
import com.banking.report.exception.InvalidDateRangeException;
import com.banking.report.repository.ReportRepository;
import java.time.LocalDate;
import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

/** Unit tests for the report business rules. */
@ExtendWith(MockitoExtension.class)
class ReportServiceImplTest {

    @Mock
    private ReportRepository reportRepository;

    @Mock
    private ReportEventPublisher eventPublisher;

    private ReportServiceImpl service;

    private UUID transactionId;

    @BeforeEach
    void setUp() {
        service = new ReportServiceImpl(reportRepository, eventPublisher);
        transactionId = UUID.randomUUID();
        lenient().when(reportRepository.save(any(ReportDocument.class)))
                .thenAnswer(inv -> Mono.just(inv.getArgument(0)));
        lenient().when(reportRepository.findByTransactionId(any(UUID.class)))
                .thenReturn(Mono.empty());
    }

    private ProductReportRequest request(final LocalDate from, final LocalDate to) {
        final ProductReportRequest req = new ProductReportRequest();
        req.setProductType(ProductType.SAVINGS);
        req.setFromDate(from);
        req.setToDate(to);
        req.setFormat(ProductReportRequest.FormatEnum.CSV);
        return req;
    }

    @Test
    void rejectsInvertedDates() {
        StepVerifier.create(service.createProductReport(transactionId,
                        request(LocalDate.of(2026, 3, 10), LocalDate.of(2026, 3, 1))))
                .expectError(InvalidDateRangeException.class)
                .verify();
    }

    @Test
    void rejectsRangeOverNinetyDays() {
        StepVerifier.create(service.createProductReport(transactionId,
                        request(LocalDate.of(2026, 1, 1), LocalDate.of(2026, 4, 15))))
                .expectError(InvalidDateRangeException.class)
                .verify();
    }

    @Test
    void acceptsSameDay() {
        StepVerifier.create(service.createProductReport(transactionId,
                        request(LocalDate.of(2026, 3, 1), LocalDate.of(2026, 3, 1))))
                .assertNext(report -> assertThat(report.getStatus()).isEqualTo("PENDING"))
                .verifyComplete();
    }

    @Test
    void acceptsExactNinetyDayRange() {
        StepVerifier.create(service.createProductReport(transactionId,
                        request(LocalDate.of(2026, 1, 1), LocalDate.of(2026, 3, 31))))
                .assertNext(report -> assertThat(report.getStatus()).isEqualTo("PENDING"))
                .verifyComplete();
    }

    @Test
    void isIdempotentByTransactionId() {
        final ReportDocument existing = ReportDocument.builder()
                .reportId(UUID.randomUUID())
                .transactionId(transactionId)
                .status("PENDING")
                .build();
        when(reportRepository.findByTransactionId(transactionId))
                .thenReturn(Mono.just(existing));

        StepVerifier.create(service.createProductReport(transactionId,
                        request(LocalDate.of(2026, 3, 1), LocalDate.of(2026, 3, 2))))
                .assertNext(report -> assertThat(report.getReportId())
                        .isEqualTo(existing.getReportId()))
                .verifyComplete();
    }

    @Test
    void cardMovementsReportDefaultsToBoth() {
        final CardMovementsReportRequest req = new CardMovementsReportRequest();
        req.setCustomerId(UUID.randomUUID());

        StepVerifier.create(service.createCardMovementsReport(transactionId, req))
                .assertNext(report -> {
                    assertThat(report.getCardType()).isEqualTo("BOTH");
                    assertThat(report.getReportType()).isEqualTo("CARD_LAST_MOVEMENTS");
                })
                .verifyComplete();
    }
}
