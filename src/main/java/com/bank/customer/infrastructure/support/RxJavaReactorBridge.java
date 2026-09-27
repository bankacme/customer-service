package com.bank.customer.infrastructure.support;

import io.reactivex.rxjava3.core.Completable;
import io.reactivex.rxjava3.core.Flowable;
import io.reactivex.rxjava3.core.Single;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

/**
 * The application layer speaks RxJava 3 (ports/use cases); the generated REST interface
 * speaks Project Reactor (WebFlux). Both sides implement {@code org.reactivestreams.Publisher},
 * so the conversion needs no extra library — same approach proven in bank-spike.
 */
public final class RxJavaReactorBridge {

    private RxJavaReactorBridge() {
    }

    public static <T> Mono<T> toMono(Single<T> single) {
        return Mono.from(single.toFlowable());
    }

    public static Mono<Void> toMono(Completable completable) {
        return Mono.from(completable.toFlowable());
    }

    public static <T> Flux<T> toFlux(Flowable<T> flowable) {
        return Flux.from(flowable);
    }
}
