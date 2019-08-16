package com.example.demo1;

import org.junit.Test;
import reactor.core.publisher.BaseSubscriber;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;
import reactor.test.publisher.TestPublisher;

import java.time.Duration;

public class ReactorTest {

    @Test
    public void test1() {
        Flux<String> source = Flux.just("John", "Monica", "Mark", "Cloe", "Frank", "Casper", "Olivia", "Emily", "Cate")
                .filter(name -> name.length() == 4)
                .map(String::toUpperCase);

        StepVerifier
                .create(source)
                .expectNext("JOHN")
                .expectNextMatches(name -> name.startsWith("MA"))
                .expectNext("CLOE", "CATE")
                .expectComplete()
                .verify();

    }

    @Test
    public void testError() {
        Flux<String> source = Flux.just("John", "Monica", "Mark", "Cloe", "Frank", "Casper", "Olivia", "Emily", "Cate")
                .filter(name -> name.length() == 4)
                .map(String::toUpperCase);


        StepVerifier
                .create(source.concatWith(Mono.error(new IllegalArgumentException("Our message"))))
                .expectNextCount(4)
                .expectErrorMatches(throwable -> throwable instanceof IllegalArgumentException &&
                        throwable.getMessage().equals("Our message")
                ).verify();


    }

    //post execution assertions
    @Test
    public void test2() {
        Flux<Integer> source = Flux.<Integer>create(emitter -> {
            emitter.next(1);
            emitter.next(2);
            emitter.next(3);
            emitter.complete();
            try {
                Thread.sleep(1000);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            emitter.next(4);
        }).filter(number -> number % 2 == 0);

        StepVerifier.create(source)
                .expectNext(2)
                .expectComplete()
                .verifyThenAssertThat()
                .hasDropped(4)
                .tookLessThan(Duration.ofMillis(1050));
    }

    public void testPublisher() {
        TestPublisher
                .<String>create()
                .next("First", "Second", "Third")
                .error(new RuntimeException("Message"));

        TestPublisher
                .createNoncompliant(TestPublisher.Violation.ALLOW_NULL)
                .emit("1", "2", null, "3");

//        In addition to ALLOW_NULL, we can also use TestPublisher.Violation to:
//
//        REQUEST_OVERFLOW – allows calling next() without throwing an IllegalStateException when there’s an insufficient number of requests
//        CLEANUP_ON_TERMINATE – allows sending any termination signal several times in a row
//        DEFER_CANCELLATION – allows us to ignore cancellation signals and continue with emitting elements
    }

    @Test
    public void testUppercaseConverter() {
        final TestPublisher<String> testPublisher = TestPublisher.create();

        UppercaseConverter uppercaseConverter = new UppercaseConverter(testPublisher.flux());

        StepVerifier.create(uppercaseConverter.getUpperCase())
                .then(() -> testPublisher.emit("aA", "bb", "ccc"))
                .expectNext("AA", "BB", "CCC")
                .verifyComplete();
    }
}
