package com.banking.report.event;

import com.banking.report.document.ReportDocument;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.core.reactive.ReactiveKafkaProducerTemplate;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Mono;

/**
 * Publishes report lifecycle events to banking.report.events.v1.
 * Payloads are provisional until formalized in the Kafka catalog.
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class ReportEventPublisher {

    /** Topic defined by the skill contract. */
    public static final String TOPIC = "banking.report.events.v1";

    private final ReactiveKafkaProducerTemplate<String, Object> kafkaTemplate;

    /** Publishes ReportRequested for the given report. Never blocks the caller. */
    public void publishReportRequested(final ReportDocument report) {
        final Map<String, Object> event = new HashMap<>();
        event.put("eventId", UUID.randomUUID().toString());
        event.put("eventType", "ReportRequested");
        event.put("reportId", report.getReportId().toString());
        event.put("reportType", report.getReportType());
        event.put("status", report.getStatus());
        kafkaTemplate.send(TOPIC, report.getReportId().toString(), event)
                .doOnSuccess(v -> log.debug("ReportRequested published for reportId={}",
                        report.getReportId()))
                .doOnError(e -> log.error("Failed to publish ReportRequested for reportId={}",
                        report.getReportId(), e))
                .onErrorResume(e -> Mono.empty())
                .subscribe();
    }
}
