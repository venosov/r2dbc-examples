package com.example.demo;

import io.r2dbc.spi.ConnectionFactory;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.web.reactive.server.WebTestClient;
import reactor.test.StepVerifier;

/**
 * docker run --name some-postgres -e POSTGRES_PASSWORD=mysecretpassword -d -p 5432:5432 postgres
 */
@SpringBootTest
public class DemoApplicationTests {
    @Autowired
    private LoginEventRepository loginEventRepository;
    @Autowired
    private ConnectionFactory connectionFactory;

    @Test
    public void login() {
        WebTestClient client = WebTestClient.bindToController(new LoginController(loginEventRepository,
                connectionFactory)).build();
        client.post()
                .uri("/login/victor")
                .exchange()
                .expectStatus()
                .isOk();
    }
}
