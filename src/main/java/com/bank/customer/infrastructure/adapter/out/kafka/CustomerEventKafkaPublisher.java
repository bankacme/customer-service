package com.bank.customer.infrastructure.adapter.out.kafka;

import com.bank.customer.application.port.out.CustomerEventPublisherPort;
import com.bank.customer.domain.event.CustomerDomainEvent;
import io.reactivex.rxjava3.core.Completable;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

/**
 * No-op until P3 (Kafka). Keeps the shape of the real thing so use cases never change
 * when this gets swapped for a real Kafka producer later — only this class changes.
 */
@Component
@Slf4j
public class CustomerEventKafkaPublisher implements CustomerEventPublisherPort {

    @Override
    public Completable publish(CustomerDomainEvent event) {
        log.debug("[no-op, until P3] would publish {}", event);
        return Completable.complete();
    }
}
