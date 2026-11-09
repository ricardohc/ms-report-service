package com.banking.report.repository;

import com.banking.report.document.MovementProjectionDocument;
import org.springframework.data.repository.reactive.ReactiveCrudRepository;
import org.springframework.stereotype.Repository;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@Repository
public interface MovementProjectionRepository
        extends ReactiveCrudRepository<MovementProjectionDocument, String> {

    Mono<MovementProjectionDocument> findByEventId(String eventId);

    Flux<MovementProjectionDocument> findByProductIdAndOccurredAtBetweenOrderByOccurredAtDesc(
            java.util.UUID productId, java.time.LocalDateTime from, java.time.LocalDateTime to);

    Flux<MovementProjectionDocument> findTop10ByCustomerIdAndCardTypeOrderByOccurredAtDesc(
            java.util.UUID customerId, String cardType);
}
