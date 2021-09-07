package com.example.application;

import com.example.application.reactivedatabase.model.Book;
import com.example.application.reactivedatabase.service.ReactiveBookService;
import lombok.extern.log4j.Log4j2;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.SpringBootTest.WebEnvironment;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.context.TestPropertySource;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import java.util.Arrays;
import java.util.function.Predicate;

import static org.mockito.ArgumentMatchers.anyString;

@Log4j2
@SpringBootTest(webEnvironment = WebEnvironment.RANDOM_PORT)
@DisplayName("<= Book Reactive Test with Mockito =>")
@TestPropertySource(properties = "spring.autoconfigure.exclude=org.springframework.boot.autoconfigure.mongo.embedded.EmbeddedMongoAutoConfiguration")
class BookReactiveMockTest {

    @MockBean
    private ReactiveBookService service;

    private Book book = new Book("1","Ruben de noche","Ruben0x52");
    private Book book2 = new Book("2","Diamantes","No c");

    @BeforeEach
    void setup() {
        Mono<Book> monoJustOrEmpty = Mono.just(book);
        Mockito.when(service.findById(anyString())).thenReturn(Mono.just(monoJustOrEmpty));
        Mockito.when(service.findByTitle("Ruben de noche")).thenReturn(Mono.just(book));
        Mockito.when(service.findByAuthor("Ruben0x52")).thenReturn(Mono.just(book));
        Mockito.when(service.findAll()).thenReturn(Flux.just(Arrays.asList(book, book2)));
    }

    @Test
    @DisplayName("Testing findById")
    void findById() {

        final Predicate<Book> predicate = autor -> autor.getId().equalsIgnoreCase("1");

        Mono<Book> mono = this.service.findById("1")
                .flatMap(bookMono -> Mono.just(book));

        StepVerifier.create(mono)
                .expectNextMatches(predicate)
                .verifyComplete();
   }

   @Test
   @DisplayName("Testing find by title")
   void findByTitle() {
       final Predicate<Book> predicate = autor -> autor.getTitle().equalsIgnoreCase("Ruben de noche");

       Mono<Book> mono = this.service.findByTitle("Ruben de noche");

       StepVerifier.create(mono)
               .expectNextMatches(predicate)
               .verifyComplete();
   }

    @Test
    @DisplayName("Testing find by author")
    void findByAuthor() {
        final Predicate<Book> predicate = autor -> autor.getAuthor().equalsIgnoreCase("Ruben0x52");

        Mono<Book> mono = this.service.findByAuthor("Ruben0x52");

        StepVerifier.create(mono)
                .expectNextMatches(predicate)
                .verifyComplete();
    }

    @Test
    @DisplayName("Testing find all books")
    void findByAll() {

        StepVerifier.create(this.service.findAll())
                .expectNextCount(1L)
                .verifyComplete();
    }

}
