/*
 * Copyright 2019 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.example.demo;

import java.time.LocalDateTime;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;

import io.r2dbc.postgresql.api.Notification;
import io.r2dbc.postgresql.api.PostgresqlConnection;
import io.r2dbc.postgresql.api.PostgresqlResult;
import io.r2dbc.spi.ConnectionFactory;
import io.r2dbc.spi.Wrapped;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.core.io.ClassPathResource;
import org.springframework.data.annotation.Id;
import org.springframework.data.r2dbc.connectionfactory.init.ConnectionFactoryInitializer;
import org.springframework.data.r2dbc.connectionfactory.init.ResourceDatabasePopulator;
import org.springframework.data.r2dbc.repository.config.EnableR2dbcRepositories;
import org.springframework.data.relational.core.mapping.Column;
import org.springframework.data.relational.core.mapping.Table;
import org.springframework.data.repository.reactive.ReactiveCrudRepository;
import org.springframework.http.MediaType;

@SpringBootApplication
@EnableR2dbcRepositories(considerNestedRepositories = true)
public class DemoApplication {

	public static void main(String[] args) {
		SpringApplication.run(DemoApplication.class, args);
	}

	@Bean
	ConnectionFactoryInitializer initializer(ConnectionFactory connectionFactory) {

		ConnectionFactoryInitializer initializer = new ConnectionFactoryInitializer();
		initializer.setConnectionFactory(connectionFactory);

		ResourceDatabasePopulator populator = new ResourceDatabasePopulator(new ClassPathResource("schema.sql"));
		populator.setSeparator(";;");
		initializer.setDatabasePopulator(populator);

		return initializer;
	}
}

@RestController
class LoginController {

	final LoginEventRepository repository;
	final PostgresqlConnection connection;

	public LoginController(LoginEventRepository repository, ConnectionFactory connectionFactory) {

		this.repository = repository;
		this.connection = Mono.from(connectionFactory.create())
				.map(it -> (PostgresqlConnection) ((Wrapped) it).unwrap()).block();
	}

	@PostConstruct
	private void postConstruct() {
		this.connection.createStatement("LISTEN login_event_notification")
				.execute()
				.flatMap(PostgresqlResult::getRowsUpdated)
				.subscribe();
	}

	@PreDestroy
	private void preDestroy() {
		this.connection.close().subscribe();
	}

	@PostMapping("/login/{username}")
	Mono<Void> login(@PathVariable String username) {
		return this.repository
				.save(new LoginEvent(username, LocalDateTime.now()))
				.then();
	}

	@GetMapping(value = "/login-stream", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
	Flux<CharSequence> getStream() {
		return this.connection
				.getNotifications()
				.map(Notification::getParameter);
	}
}

interface LoginEventRepository extends ReactiveCrudRepository<LoginEvent, Integer> {

}

@Table
class LoginEvent {

	@Id
	Integer id;

	@Column("user_name")
	String username;

	LocalDateTime loginTime;

	public LoginEvent(String username, LocalDateTime loginTime) {
		this.username = username;
		this.loginTime = loginTime;
	}

}
