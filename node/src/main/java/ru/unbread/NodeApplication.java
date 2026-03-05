package ru.unbread;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;

@EnableJpaRepositories("ru.unbread.*")
@EntityScan("ru.unbread.*")
@ComponentScan("ru.unbread.*"
@SpringBootApplication
public class NodeApplication {

    public static void main(String[] args) {
        SpringApplication.run(NodeApplication.class);
    }
}
