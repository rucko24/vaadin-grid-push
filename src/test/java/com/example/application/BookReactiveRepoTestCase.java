package com.example.application;

import com.example.application.reactivedatabase.model.Book;
import com.example.application.reactivedatabase.repository.BookReactiveRepository;
import lombok.extern.log4j.Log4j2;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import reactor.core.publisher.Flux;
import reactor.test.StepVerifier;

import java.time.Duration;
import java.util.Arrays;

/**
 *
 */
@Log4j2
@SpringBootTest
@DisplayName("<= Book reactive mongo with local configuration on docker =>")
class BookReactiveRepoTestCase {

    @Autowired
    private BookReactiveRepository reactiveBookService;

    @BeforeEach
    @DisplayName("Delete all with 1 second delay")
    void deleteAll() {

        StepVerifier.create(reactiveBookService.deleteAll()
                .delayElement(Duration.ofSeconds(1))
                .log("Delete all"))
                .expectNextCount(0)
                .verifyComplete();

    }

    @Test
    @DisplayName("Save some books of programming")
    void save() {

        StepVerifier.create(reactiveBookService
                .saveAll(Flux.fromIterable(Arrays.asList(
                        new Book("", "Clean Code", "Rober C. Martin"),
                        new Book("", "BDD IN ACTION", "John Smart"),
                        new Book("", "El feliz abrazo de una madre mocha", "El cuñao"),
                        new Book("", "ITEXT IN ACTION", "Bruno Lowagie"),
                        new Book("", "SPRING BOOT IN ACTION", "Craig Walls"),
                        new Book("", "Kubernetes in Action", "Marko Luksa"),
                        new Book("", "La fuga de los caballos paraliticos", "El cuñao"),
                        new Book("", "Clean Architecture", "Rober C. Martin"))
                        )
                )
                .delayElements(Duration.ofSeconds(1)))
                .expectNextCount(8)
                .verifyComplete();

    }

}
