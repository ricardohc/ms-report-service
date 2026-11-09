package com.banking.report.repository;

import java.util.UUID;

import com.banking.report.document.ReportDocument;
import org.springframework.data.repository.reactive.ReactiveCrudRepository;
import org.springframework.stereotype.Repository;
import reactor.core.publisher.Mono;

@Repository
public interface ReportRepository extends ReactiveCrudRepository<ReportDocument, UUID> {

    Mono<ReportDocument> findByTransactionId(UUID transactionId);
}
