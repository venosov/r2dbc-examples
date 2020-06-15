package com.example.demo;

import com.example.demo.DemoApplication.*;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.r2dbc.core.DatabaseClient;
import org.springframework.test.web.reactive.server.WebTestClient;
import reactor.test.StepVerifier;

@SpringBootTest
public class DemoApplicationTests {
    @Autowired
    private PersonRepository personRepository;
    @Autowired
    private PersonEventRepository personEventRepository;
    @Autowired
    private DatabaseClient databaseClient;

    @Test
    void findAll() {
        WebTestClient client = WebTestClient.bindToController(new PersonController(personRepository, personEventRepository,
                databaseClient)).build();
        client.get()
                .uri("/")
                .exchange()
                .expectStatus()
                .isOk()
                .returnResult(Person.class)
                .getResponseBody()
                .as(StepVerifier::create) //
                .assertNext(p -> p.firstName.equals("Walter")) //
                .assertNext(p -> p.firstName.equals("Jesse")) //
                .assertNext(p -> p.firstName.equals("Hank")) //
                .verifyComplete();
    }

    @Test
    void findAllEvents() {
        WebTestClient client = WebTestClient.bindToController(new PersonController(personRepository, personEventRepository,
                databaseClient)).build();
        client.get()
                .uri("/events")
                .exchange()
                .expectStatus()
                .isOk()
                .returnResult(PersonEvent.class)
                .getResponseBody()
                .as(StepVerifier::create) //
                .verifyComplete();
    }

    @Test
    void findAllByLastName() {
        WebTestClient client = WebTestClient.bindToController(new PersonController(personRepository, personEventRepository,
                databaseClient)).build();
        client.get()
                .uri("/by-name/White")
                .exchange()
                .expectStatus()
                .isOk()
                .returnResult(Person.class)
                .getResponseBody()
                .as(StepVerifier::create) //
                .assertNext(p -> p.firstName.equals("Walter")) //
                .verifyComplete();

    }
}
