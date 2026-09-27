package com.bank.customer.infrastructure.config;

import com.bank.customer.infrastructure.adapter.out.persistence.CustomerDocument;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.data.domain.Sort;
import org.springframework.data.mongodb.core.ReactiveMongoTemplate;
import org.springframework.data.mongodb.core.index.Index;
import org.springframework.stereotype.Component;

/**
 * Creates the 3 indexes from data-model.md section 2.1 explicitly at startup, same
 * approach proven in bank-spike (check #3) rather than relying on annotations +
 * {@code auto-index-creation}.
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class CustomerIndexInitializer implements ApplicationRunner {

    private final ReactiveMongoTemplate mongoTemplate;

    @Override
    public void run(ApplicationArguments args) {
        Index uniqueDocument = new Index()
                .on("document.type", Sort.Direction.ASC)
                .on("document.number", Sort.Direction.ASC)
                .unique()
                .named("uk_customer_document");

        Index status = new Index()
                .on("status", Sort.Direction.ASC)
                .named("ix_customer_status");

        Index typeProfile = new Index()
                .on("type", Sort.Direction.ASC)
                .on("profile", Sort.Direction.ASC)
                .named("ix_customer_type_profile");

        mongoTemplate.indexOps(CustomerDocument.class).createIndex(uniqueDocument)
                .subscribe(name -> log.info("Index '{}' ready", name),
                        error -> log.error("Could not create index 'uk_customer_document'", error));

        mongoTemplate.indexOps(CustomerDocument.class).createIndex(status)
                .subscribe(name -> log.info("Index '{}' ready", name),
                        error -> log.error("Could not create index 'ix_customer_status'", error));

        mongoTemplate.indexOps(CustomerDocument.class).createIndex(typeProfile)
                .subscribe(name -> log.info("Index '{}' ready", name),
                        error -> log.error("Could not create index 'ix_customer_type_profile'", error));
    }
}
