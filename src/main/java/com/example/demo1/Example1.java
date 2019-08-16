package com.example.demo1;

import org.reactivestreams.Subscriber;
import org.reactivestreams.Subscription;
import reactor.core.publisher.ConnectableFlux;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Schedulers;

import java.util.ArrayList;
import java.util.List;

import static java.time.Duration.ofSeconds;

public class Example1 {

    public static void main(String[] args) {

        Flux<String> justFlux = Flux.just(null, "2", "3");

//        combineMonoToFlux();
//        collectionElements();
//        customSubscriber();
//        backPressure();
        combiningTwoStreams();
    }

    public static void combineMonoToFlux() {
        Mono<String> foo = Mono.just("foo");
        Mono<String> bar = Mono.just("bar");
        Flux.concat(foo, bar).subscribe(System.out::println);
    }

    //The data won’t start flowing until we subscribe.
    public static void collectionElements() {
        List<Integer> elements = new ArrayList<>();

        Flux.just(1, 2, 3, 4)
                .log()
                .subscribe(elements::add);
        elements.forEach(System.out::println);
    }

    public static void customSubscriber() {
        List<Integer> elements = new ArrayList<>();
        Flux.just(1, 2, 3, 4)
                .log()
                .subscribe(new Subscriber<Integer>() {
                    @Override
                    public void onSubscribe(Subscription s) {
                        s.request(Long.MAX_VALUE);
                    }

                    @Override
                    public void onNext(Integer integer) {
                        elements.add(integer);
                    }

                    @Override
                    public void onError(Throwable t) {
                    }

                    @Override
                    public void onComplete() {
                    }
                });
        elements.forEach(System.out::println);
    }

    public static void backPressure() {
        List<Integer> elements = new ArrayList<>();
        Flux.just(1, 2, 3, 4)
                .log()
                .subscribe(new Subscriber<Integer>() {
                    private Subscription s;
                    int onNextAmount;

                    @Override
                    public void onSubscribe(Subscription s) {
                        this.s = s;
                        s.request(2);
                    }

                    @Override
                    public void onNext(Integer integer) {
                        elements.add(integer);
                        onNextAmount++;
                        if (onNextAmount % 2 == 0) {
                            s.request(2);
                        }
                    }

                    @Override
                    public void onError(Throwable t) {}

                    @Override
                    public void onComplete() {}
                });

        elements.forEach(System.out::println);
    }

    public static void combiningTwoStreams() {
        List<String> elements = new ArrayList<>();
        Flux.just(1, 2, 3, 4)
                .log()
                .map(i -> i * 2)
                .zipWith(Flux.range(0, Integer.MAX_VALUE),
                        (one, two) -> String.format("First Flux: %d, Second Flux: %d", one, two))
                .subscribe(elements::add);

        elements.forEach(System.out::println);

    }

}
