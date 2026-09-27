package com.bank.customer.infrastructure.adapter.out.persistence;

import io.reactivex.rxjava3.core.Flowable;
import io.reactivex.rxjava3.core.Maybe;
import io.reactivex.rxjava3.core.Single;
import org.springframework.data.repository.reactive.RxJava3CrudRepository;

public interface CustomerMongoRepository extends RxJava3CrudRepository<CustomerDocument, String> {

    Single<Boolean> existsByDocumentTypeAndDocumentNumber(String type, String number);

    Maybe<CustomerDocument> findByDocumentTypeAndDocumentNumber(String type, String number);

    Flowable<CustomerDocument> findByStatus(String status);

    Flowable<CustomerDocument> findByType(String type);

    Flowable<CustomerDocument> findByProfile(String profile);
}
