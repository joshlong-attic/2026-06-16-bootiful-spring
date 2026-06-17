package com.example.adoptions;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.annotation.Id;
import org.springframework.data.jdbc.core.dialect.JdbcPostgresDialect;
import org.springframework.data.repository.ListCrudRepository;
import org.springframework.resilience.annotation.ConcurrencyLimit;
import org.springframework.resilience.annotation.EnableResilientMethods;
import org.springframework.resilience.annotation.Retryable;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.service.annotation.GetExchange;
import org.springframework.web.service.registry.ImportHttpServices;

import java.util.Collection;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;

import static org.springaicommunity.mcp.security.client.sync.config.McpClientOAuth2Configurer.mcpClientOAuth2;

@EnableResilientMethods
@ImportHttpServices(CatFactsClient.class)
@SpringBootApplication
public class AdoptionsApplication {

    public static void main(String[] args) {
        SpringApplication.run(AdoptionsApplication.class, args);
    }

    @Bean
    JdbcPostgresDialect jdbcPostgresDialect (){
        return JdbcPostgresDialect.INSTANCE;
    }
}

@Controller
@ResponseBody
class CatFactsController {

    private final CatFactsClient factsClient;

    private final AtomicInteger counter = new AtomicInteger(0);

    CatFactsController(CatFactsClient factsClient) {
        this.factsClient = factsClient;
    }

    @ConcurrencyLimit(10)
    @Retryable(maxRetries = 5, includes = IllegalStateException.class)
    @GetMapping("/cats")
    CatFacts facts() {

        if (this.counter.getAndIncrement() < 5) {
            IO.println("oops!");
            throw new IllegalStateException("Simulating a failure");
        }

        IO.println("success!");
        return factsClient.facts();
    }
}

interface CatFactsClient {

    @GetExchange("https://www.catfacts.net/api")
    CatFacts facts();
}

@Controller
@ResponseBody
class AdoptionsController {

    private final DogRepository repository;


    AdoptionsController(DogRepository repository) {
        this.repository = repository;
    }

    @PostMapping("/dogs/{dogId}/adoptions")
    void adopt(@PathVariable int dogId, @RequestParam String owner) {
        this.repository.findById(dogId).ifPresent(dog -> {
            var updated = this.repository.save(
                    new Dog(dog.id(), dog.name(), owner, dog.description()));
            IO.println("Adopted " + updated.name() + " by " + owner);
        });
    }
}

record CatFact(String fact) {
}

record CatFacts(CatFact[] facts) {
}

@Controller
@ResponseBody
class DogsController {

    private final DogRepository repository;

    DogsController(DogRepository repository) {
        this.repository = repository;
    }

    @GetMapping(value = "/dogs", version = "2.0")
    Collection<Dog> dogs() {
        return repository.findAll();
    }

    @GetMapping(value = "/dogs", version = "1.0")
    Collection<Map<String, Object>> dogsv1() {
        return repository.findAll()
                .stream()
                .map(d -> Map.of("fullName", (Object) d.name(),
                        "id", d.id()))
                .toList();
    }
}

interface DogRepository extends ListCrudRepository<Dog, Integer> {
}

record Dog(@Id int id, String name, String owner, String description) {
}

@Configuration
class SecurityConfiguration {

    @Bean
    Customizer<HttpSecurity> httpSecurityCustomizer() {
        return http -> http.with(mcpClientOAuth2());
    }
}
