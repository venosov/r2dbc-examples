package com.example.demo;

import com.example.demo.DemoApplication.LoginEventRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import reactor.test.StepVerifier;

/**
 * docker run --name some-postgres -e POSTGRES_PASSWORD=mysecretpassword -d -p 5432:5432 postgres
 */
@SpringBootTest(classes = InfrastructureConfiguration.class)
public class LoginEventRepositoryTests {
    private final LoginEventRepository loginEventRepository;

    public LoginEventRepositoryTests(@Autowired LoginEventRepository loginEventRepository) {
        this.loginEventRepository = loginEventRepository;
    }

    @Test
    public void executesFindAll() {
        loginEventRepository.findAll() //
                .as(StepVerifier::create) //
                .verifyComplete();
    }
}
