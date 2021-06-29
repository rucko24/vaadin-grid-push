package com.example.application;

import com.example.application.reactivedatabase.model.Book;
import com.example.application.reactivedatabase.repository.BookReactiveRepository;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.web.servlet.support.SpringBootServletInitializer;
import org.springframework.context.annotation.Bean;
import org.vaadin.artur.helpers.LaunchUtil;
import reactor.core.publisher.Flux;

import java.util.Arrays;

/**
 * The entry point of the Spring Boot application.
 */
@SpringBootApplication
public class Application extends SpringBootServletInitializer {

    public static void main(String[] args) {
        LaunchUtil.launchBrowserInDevelopmentMode(SpringApplication.run(Application.class, args));
    }

    @Bean
    public CommandLineRunner fillRepoWithSomeBooks(final BookReactiveRepository reactiveRepository) {
        return (e -> {
            reactiveRepository
                    .deleteAll()
                    .thenMany(reactiveRepository.saveAll(Flux.fromIterable(Arrays.asList(
                            new Book("", "Clean Code", "Rober C. Martin"),
                            new Book("", "BDD IN ACTION", "John Smart"),
                            new Book("", "El feliz abrazo de una madre mocha", "El cuñao"),
                            new Book("", "ITEXT IN ACTION", "Bruno Lowagie"),
                            new Book("", "SPRING BOOT IN ACTION", "Craig Walls"),
                            new Book("", "Kubernetes in Action", "Marko Luksa"),
                            new Book("", "La fuga de los caballos paraliticos", "El cuñao"),
                            new Book("", "Clean Architecture", "Rober C. Martin")))))
                    .log()
                    .subscribe();
        });
    }

}
