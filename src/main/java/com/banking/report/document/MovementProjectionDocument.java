package com.banking.report.document;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.CompoundIndex;
import org.springframework.data.mongodb.core.mapping.Document;

/**
 * Projection of movements fed exclusively by domain events (Parte III).
 * Deduplicated by eventId.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Document(collection = "movement_projections")
@CompoundIndex(name = "ux_event_id", def = "{'eventId': 1}", unique = true)
public class MovementProjectionDocument {

    @Id
    private String id;

    /** Event envelope id used for deduplication. */
    private String eventId;

    private String topic;

    private String aggregateId;

    private UUID customerId;

    private UUID productId;

    /** CREDIT or DEBIT. Required for the card movements report. */
    private String cardType;

    private String movementType;

    private BigDecimal amount;

    private LocalDateTime occurredAt;

    private LocalDateTime projectedAt;
}