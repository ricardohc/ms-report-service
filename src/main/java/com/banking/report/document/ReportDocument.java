package com.banking.report.document;

import java.time.LocalDateTime;
import java.util.UUID;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;

/**
 * Report aggregate persisted in the banking_report database.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Document(collection = "reports")
public class ReportDocument {

    @Id
    private UUID reportId;

    /** X-Transaction-ID used for idempotency (unique). */
    @Indexed(unique = true)
    private UUID transactionId;

    private String reportType;

    private String productType;

    private String cardType;

    private UUID customerId;

    private LocalDateTime fromDate;

    private LocalDateTime toDate;

    private String format;

    /** PENDING, PROCESSING, COMPLETED, FAILED. */
    private String status;

    private LocalDateTime generatedAt;

    private String downloadUrl;

    private String errorMessage;
}
